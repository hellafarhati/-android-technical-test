package fr.leboncoin.data.usecase

import fr.leboncoin.data.repository.AlbumsRepository
import fr.leboncoin.core.model.RefreshResult
import javax.inject.Inject

/** Declenche une synchronisation reseau vers le cache local. */
class RefreshAlbumsUseCase @Inject constructor(
    private val repository: AlbumsRepository,
) {
    suspend operator fun invoke(): RefreshResult = repository.refresh()
}
