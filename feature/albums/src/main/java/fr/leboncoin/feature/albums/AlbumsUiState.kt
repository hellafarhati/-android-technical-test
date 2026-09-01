package fr.leboncoin.feature.albums

import androidx.compose.runtime.Immutable
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.AlbumGroup
import fr.leboncoin.core.model.DataError

/**
 * Modeles dedies a l'UI.
 *
 * Ils sont annotes `@Immutable` : le compilateur Compose peut alors sauter les recompositions
 * inutiles. Les modeles du domaine vivent dans un module Kotlin pur (sans compilateur Compose),
 * ils seraient consideres comme instables s'ils etaient utilises directement dans les composables.
 */
@Immutable
data class AlbumUi(
    val id: Int,
    val albumId: Int,
    val title: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean,
)

@Immutable
data class AlbumGroupUi(
    val albumId: Int,
    val photos: List<AlbumUi>,
)

@Immutable
data class AlbumsUiState(
    /** Premier chargement, aucune donnee encore disponible. */
    val isLoading: Boolean = true,
    /** Synchronisation en arriere-plan alors que du contenu est deja affiche. */
    val isRefreshing: Boolean = false,
    val groups: List<AlbumGroupUi> = emptyList(),
    val query: String = "",
    val filter: AlbumsFilter = AlbumsFilter.ALL,
    /** Erreur de la derniere synchronisation, non consommee par l'UI. */
    val error: DataError? = null,
) {
    /** Vrai si l'utilisateur a restreint la liste (recherche ou favoris). */
    val isFiltered: Boolean get() = query.isNotBlank() || filter == AlbumsFilter.FAVORITES

    val hasContent: Boolean get() = groups.isNotEmpty()

    val photoCount: Int get() = groups.sumOf { it.photos.size }
}

internal fun Album.toUi(): AlbumUi = AlbumUi(
    id = id,
    albumId = albumId,
    title = title,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)

internal fun AlbumGroup.toUi(): AlbumGroupUi = AlbumGroupUi(
    albumId = albumId,
    photos = photos.map { it.toUi() },
)
