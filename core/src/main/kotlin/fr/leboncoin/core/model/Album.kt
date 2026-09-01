package fr.leboncoin.core.model

/**
 * Modele metier d'une photo d'album.
 *
 * decouple du DTO reseau (AlbumDto) et de
 * l'entite Room : le contrat de l'API peut changer sans impacter l'UI, et l'UI peut evoluer
 * sans forcer une migration de base.
 */
data class Album(
    /** Identifiant unique de la photo. */
    val id: Int,
    /** Identifiant de l'album auquel la photo appartient. */
    val albumId: Int,
    val title: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false,
)

/** Regroupement des photos par album*/
data class AlbumGroup(
    val albumId: Int,
    val photos: List<Album>,
)
