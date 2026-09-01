package fr.leboncoin.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.leboncoin.data.database.entity.AlbumEntity
import fr.leboncoin.data.database.entity.PopulatedAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumsDao {

    @Query(
        """
        SELECT albums.*, (favorites.photo_id IS NOT NULL) AS is_favorite
        FROM albums
        LEFT JOIN favorites ON favorites.photo_id = albums.id
        ORDER BY albums.album_id ASC, albums.id ASC
        """,
    )
    fun observeAlbums(): Flow<List<PopulatedAlbum>>

    @Query(
        """
        SELECT albums.*, (favorites.photo_id IS NOT NULL) AS is_favorite
        FROM albums
        LEFT JOIN favorites ON favorites.photo_id = albums.id
        WHERE albums.id = :id
        LIMIT 1
        """,
    )
    fun observeAlbum(id: Int): Flow<List<PopulatedAlbum>>

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun count(): Int


    @Upsert
    suspend fun upsertAll(albums: List<AlbumEntity>)
}
