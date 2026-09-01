package fr.leboncoin.data.usecase

import fr.leboncoin.data.repository.AlbumsRepository
import javax.inject.Inject

/** Ajoute ou retire une photo des favoris ; la valeur est persistee immediatement. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumsRepository,
) {
    suspend operator fun invoke(id: Int, isFavorite: Boolean) {
        repository.setFavorite(photoId = id, isFavorite = isFavorite)
    }
}
