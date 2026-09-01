package fr.leboncoin.feature.albumdetails

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.data.usecase.ObserveAlbumUseCase
import fr.leboncoin.data.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.model.Album
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class AlbumDetailUi(
    val id: Int,
    val albumId: Int,
    val title: String,
    val imageUrl: String,
    val isFavorite: Boolean,
)

@Immutable
data class AlbumDetailsUiState(
    val isLoading: Boolean = true,
    val album: AlbumDetailUi? = null,
) {
    /** L'element n'existe pas (ou plus) en base : for example after clearing cache */
    val isNotFound: Boolean get() = !isLoading && album == null
}

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    observeAlbum: ObserveAlbumUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * L'identifiant provient de la route type-safe : la cle correspond au nom de la propriete
     * de [fr.leboncoin.feature.albumdetails.navigation.AlbumDetailsDestination].
     * On le lit explicitement pour garder le ViewModel testable sans NavController.
     */
    private val photoId: Int = requireNotNull(savedStateHandle.get<Int>(ARG_PHOTO_ID)) {
        "Missing '$ARG_PHOTO_ID' argument for the album details screen"
    }

    val uiState: StateFlow<AlbumDetailsUiState> = observeAlbum(photoId)
        .map { album -> AlbumDetailsUiState(isLoading = false, album = album?.toUi()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = AlbumDetailsUiState(),
        )

    fun onToggleFavorite() {
        val current = uiState.value.album ?: return
        viewModelScope.launch {
            toggleFavorite(id = current.id, isFavorite = !current.isFavorite)
        }
    }

    companion object {
        const val ARG_PHOTO_ID = "photoId"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun Album.toUi(): AlbumDetailUi = AlbumDetailUi(
    id = id,
    albumId = albumId,
    title = title,
    // On affiche l'image pleine resolution si disponible, sinon lee thumbnail.
    imageUrl = imageUrl.ifBlank { thumbnailUrl },
    isFavorite = isFavorite,
)
