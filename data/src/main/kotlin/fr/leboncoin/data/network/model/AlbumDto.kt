package fr.leboncoin.data.network.model

import kotlinx.serialization.Serializable

/**
 * Representation de la reponse HTTP.
 * on accorde une valeur par defaut a toutes les chaines , sinon, une entree
  incomplete ou avec un champ `null` ne fait plus echouer le parsing des 5 000 elements.
 */
@Serializable
data class AlbumDto(
    val id: Int,
    val albumId: Int,
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)
