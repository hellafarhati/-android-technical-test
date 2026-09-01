package fr.leboncoin.data.usecase

import fr.leboncoin.data.repository.AlbumsRepository
import fr.leboncoin.core.model.Album
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observe une photo pour l'ecran de detail (le favori se met a jour en direct). */
class ObserveAlbumUseCase @Inject constructor(
    private val repository: AlbumsRepository,
) {
    operator fun invoke(id: Int): Flow<Album?> = repository.observeAlbum(id)
}
