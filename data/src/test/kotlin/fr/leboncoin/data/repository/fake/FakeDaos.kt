package fr.leboncoin.data.repository.fake

import fr.leboncoin.data.database.dao.AlbumsDao
import fr.leboncoin.data.database.dao.FavoritesDao
import fr.leboncoin.data.database.entity.AlbumEntity
import fr.leboncoin.data.database.entity.FavoriteEntity
import fr.leboncoin.data.database.entity.PopulatedAlbum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Base en memoire qui reproduit le comportement observable de Room :
 * jointure albums/favoris, tri, et re-emission a chaque ecriture.
 */
class FakeAlbumsStore : AlbumsDao, FavoritesDao {

    private data class Snapshot(
        val albums: Map<Int, AlbumEntity> = emptyMap(),
        val favorites: Set<Int> = emptySet(),
    )

    private val state = MutableStateFlow(Snapshot())

    private fun Snapshot.populated(): List<PopulatedAlbum> = albums.values
        .sortedWith(compareBy({ it.albumId }, { it.id }))
        .map { PopulatedAlbum(album = it, isFavorite = it.id in favorites) }

    override fun observeAlbums(): Flow<List<PopulatedAlbum>> = state.map { it.populated() }

    override fun observeAlbum(id: Int): Flow<List<PopulatedAlbum>> =
        state.map { snapshot -> snapshot.populated().filter { it.album.id == id } }

    override suspend fun count(): Int = state.value.albums.size

    override suspend fun upsertAll(albums: List<AlbumEntity>) {
        state.value = state.value.copy(
            albums = state.value.albums + albums.associateBy(AlbumEntity::id),
        )
    }

    override suspend fun add(favorite: FavoriteEntity) {
        state.value = state.value.copy(favorites = state.value.favorites + favorite.photoId)
    }

    override suspend fun remove(photoId: Int) {
        state.value = state.value.copy(favorites = state.value.favorites - photoId)
    }

    override fun observeCount(): Flow<Int> = state.map { it.favorites.size }
}
