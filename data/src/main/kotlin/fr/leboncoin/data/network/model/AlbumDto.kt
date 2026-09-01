package fr.leboncoin.data.network.model

import kotlinx.serialization.Serializable

/**
 * Representation brute de la reponse HTTP.
 *
 * Toutes les chaines ont une valeur par defaut : combinee a `coerceInputValues`, une entree
 * incomplete ou avec un champ `null` ne fait plus echouer le parsing des 5 000 elements.
 */
@Serializable
data class AlbumDto(
    val id: Int,
    val albumId: Int,
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)
