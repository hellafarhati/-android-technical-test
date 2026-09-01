package fr.leboncoin.data.repository

import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.RefreshResult
import kotlinx.coroutines.flow.Flow
interface AlbumsRepository {

    /**
     * Flux observable de la source de verite locale (base de donnees)
     * Emet a chaque modification : synchronisation reseau ou mise en favori
     */
    fun observeAlbums(): Flow<List<Album>>

    /** Flux d'une photo unique ; emet `null` si elle n'existe pas (ou plus) en base */
    fun observeAlbum(id: Int): Flow<Album?>

    /** Rafraichit le cache local depuis le reseau */
    suspend fun refresh(): RefreshResult

    /** Ajoute ou retire une photo des favoris (persiste localement) */
    suspend fun setFavorite(photoId: Int, isFavorite: Boolean)
}
