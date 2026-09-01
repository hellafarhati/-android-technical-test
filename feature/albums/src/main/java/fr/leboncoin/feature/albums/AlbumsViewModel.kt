package fr.leboncoin.feature.albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.data.usecase.AlbumsCriteria
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.data.usecase.ObserveAlbumsUseCase
import fr.leboncoin.data.usecase.RefreshAlbumsUseCase
import fr.leboncoin.data.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.model.DataError
import fr.leboncoin.core.model.RefreshResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    observeAlbums: ObserveAlbumsUseCase,
    private val refreshAlbums: RefreshAlbumsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * Recherche et filtre transitent par le [SavedStateHandle] : ils survivent a une rotation
     * *et* a une mort du processus, sans avoir a bloquer les changements de configuration.
     */
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val filterName: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_FILTER, AlbumsFilter.ALL.name)

    private val syncState = MutableStateFlow(SyncState())

    private val criteria = combine(
        // Pas de debounce sur une requete vide : l'affichage initial reste instantane.
        query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS },
        filterName,
    ) { text, filter ->
        AlbumsCriteria(query = text, filter = filter.toAlbumsFilter())
    }

    val uiState: StateFlow<AlbumsUiState> = combine(
        observeAlbums(criteria),
        query,
        filterName,
        syncState,
    ) { groups, currentQuery, currentFilter, sync ->
        AlbumsUiState(
            isLoading = !sync.hasLoadedOnce && groups.isEmpty(),
            isRefreshing = sync.isSyncing,
            groups = groups.map { it.toUi() },
            query = currentQuery,
            filter = currentFilter.toAlbumsFilter(),
            error = sync.error,
        )
    }.stateIn(
        scope = viewModelScope,
        // L'etat est conserve 5 s apres la disparition du dernier collecteur : une rotation
        // ne relance ni la requete SQL ni le regroupement des 5 000 elements.
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = AlbumsUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        // Garde-fou : pas de synchronisations concurrentes (double tap, retour d'ecran...).
        if (syncState.value.isSyncing) return
        syncState.update { it.copy(isSyncing = true, error = null) }
        viewModelScope.launch {
            val result = refreshAlbums()
            syncState.update {
                it.copy(
                    isSyncing = false,
                    hasLoadedOnce = true,
                    error = (result as? RefreshResult.Failure)?.error,
                )
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
    }

    fun onFilterChange(filter: AlbumsFilter) {
        savedStateHandle[KEY_FILTER] = filter.name
    }

    fun onToggleFavorite(album: AlbumUi) {
        viewModelScope.launch {
            toggleFavorite(id = album.id, isFavorite = !album.isFavorite)
        }
    }

    /** L'UI signale que l'erreur a ete presentee (snackbar) : on evite de la rejouer. */
    fun onErrorShown() {
        syncState.update { it.copy(error = null) }
    }

    private data class SyncState(
        val isSyncing: Boolean = false,
        val hasLoadedOnce: Boolean = false,
        val error: DataError? = null,
    )

    private companion object {
        const val KEY_QUERY = "albums_query"
        const val KEY_FILTER = "albums_filter"
        const val SEARCH_DEBOUNCE_MS = 250L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun String.toAlbumsFilter(): AlbumsFilter =
    AlbumsFilter.entries.firstOrNull { it.name == this } ?: AlbumsFilter.ALL
