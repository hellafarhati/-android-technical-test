package fr.leboncoin.data.usecase

import fr.leboncoin.core.dispatcher.DispatcherProvider
import fr.leboncoin.data.usecase.AlbumsCriteria
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.data.repository.AlbumsRepository
import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.AlbumGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Combine la source de verite locale et les criteres introduites par l' utilisateur pour produire la liste
 * regroupee par album.
 *
 traitement  (heavy work)  executé donc sur le dispatcher DispatcherProvider.default et pas sur le thread principal
 */
class ObserveAlbumsUseCase @Inject constructor(
    private val repository: AlbumsRepository,
    private val dispatchers: DispatcherProvider,
) {
    operator fun invoke(criteria: Flow<AlbumsCriteria>): Flow<List<AlbumGroup>> =
        combine(repository.observeAlbums(), criteria) { albums, applied ->
            albums.applyCriteria(applied)
        }.flowOn(dispatchers.default)
}

internal fun List<Album>.applyCriteria(criteria: AlbumsCriteria): List<AlbumGroup> {
    val query = criteria.query.trim()
    return asSequence()
        .filter { criteria.filter != AlbumsFilter.FAVORITES || it.isFavorite }
        .filter { query.isEmpty() || it.title.contains(query, ignoreCase = true) }
        .groupBy(Album::albumId)
        .map { (albumId, photos) -> AlbumGroup(albumId = albumId, photos = photos) }
        .sortedBy(AlbumGroup::albumId)
}
