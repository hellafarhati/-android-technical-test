package fr.leboncoin.feature.albumdetails.testing

import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.RefreshResult
import fr.leboncoin.data.repository.AlbumsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Faux repository par le test.
 *
 * Choix assume : des fakes plutot qu'une librairie de mocks. Ils sont typesafe, refactorables,
 * et ils forcent a garder les interfaces petites.
 */
class FakeAlbumsRepository(
    initialAlbums: List<Album> = emptyList(),
) : AlbumsRepository {

    private val albums = MutableStateFlow(initialAlbums)

    var refreshResult: RefreshResult = RefreshResult.Success

    var refreshCount: Int = 0
        private set

    override fun observeAlbums(): Flow<List<Album>> = albums

    override fun observeAlbum(id: Int): Flow<Album?> =
        albums.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refresh(): RefreshResult {
        refreshCount++
        return refreshResult
    }

    override suspend fun setFavorite(photoId: Int, isFavorite: Boolean) {
        albums.value = albums.value.map { album ->
            if (album.id == photoId) album.copy(isFavorite = isFavorite) else album
        }
    }

    fun emit(newAlbums: List<Album>) {
        albums.value = newAlbums
    }
}