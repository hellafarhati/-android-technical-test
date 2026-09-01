package fr.leboncoin.data.repository

import fr.leboncoin.core.dispatcher.DispatcherProvider
import fr.leboncoin.data.mapper.toDomain
import fr.leboncoin.data.mapper.toEntity
import fr.leboncoin.data.database.dao.AlbumsDao
import fr.leboncoin.data.database.dao.FavoritesDao
import fr.leboncoin.data.database.entity.FavoriteEntity
import fr.leboncoin.data.repository.AlbumsRepository
import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.DataError
import fr.leboncoin.core.model.RefreshResult
import fr.leboncoin.data.network.datasource.AlbumsRemoteDataSource
import fr.leboncoin.data.network.datasource.RemoteException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation "single source of truth" :
 *
 * - l'UI n'observe que la base locale, donc l'application fonctionne hors ligne et apres
 *   redemarrage ;
 * - le reseau est un simple mecanisme d'alimentation du cache ;
 * - un echec de synchronisation n'efface jamais les donnees deja disponibles.
 */
@Singleton
internal class AlbumsRepositoryImpl @Inject constructor(
    private val remoteDataSource: AlbumsRemoteDataSource,
    private val albumsDao: AlbumsDao,
    private val favoritesDao: FavoritesDao,
    private val dispatchers: DispatcherProvider,
) : AlbumsRepository {

    override fun observeAlbums(): Flow<List<Album>> =
        albumsDao.observeAlbums()
            .map { populated -> populated.map { it.toDomain() } }
            .flowOn(dispatchers.default)

    override fun observeAlbum(id: Int): Flow<Album?> =
        albumsDao.observeAlbum(id)
            .map { populated -> populated.firstOrNull()?.toDomain() }
            .flowOn(dispatchers.default)

    override suspend fun refresh(): RefreshResult = withContext(dispatchers.io) {
        try {
            val remoteAlbums = remoteDataSource.fetchAlbums()
            if (remoteAlbums.isEmpty()) {
                // Reponse valide mais vide : on conserve le cache existant.
                RefreshResult.Failure(DataError.EMPTY)
            } else {
                albumsDao.upsertAll(remoteAlbums.map { it.toEntity() })
                RefreshResult.Success
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (remote: RemoteException) {
            RefreshResult.Failure(remote.toDataError())
        } catch (throwable: Throwable) {
            RefreshResult.Failure(DataError.UNKNOWN)
        }
    }

    override suspend fun setFavorite(photoId: Int, isFavorite: Boolean) {
        withContext(dispatchers.io) {
            if (isFavorite) {
                favoritesDao.add(FavoriteEntity(photoId = photoId))
            } else {
                favoritesDao.remove(photoId)
            }
        }
    }
}

private fun RemoteException.toDataError(): DataError = when (this) {
    is RemoteException.Network -> DataError.NETWORK
    is RemoteException.Server -> DataError.SERVER
    is RemoteException.Serialization -> DataError.PARSING
    is RemoteException.Unknown -> DataError.UNKNOWN
}
