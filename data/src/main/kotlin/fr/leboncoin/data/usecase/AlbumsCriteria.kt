package fr.leboncoin.data.usecase

/** Filtre applique a la liste. */
enum class AlbumsFilter { ALL, FAVORITES }

/** Criteres de recherche/filtrage saisis par l'utilisateur. */
data class AlbumsCriteria(
    val query: String = "",
    val filter: AlbumsFilter = AlbumsFilter.ALL,
)
