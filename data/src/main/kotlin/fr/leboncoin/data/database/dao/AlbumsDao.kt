package fr.leboncoin.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.leboncoin.data.database.entity.AlbumEntity
import fr.leboncoin.data.database.entity.PopulatedAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumsDao {

    /**
     * Le tri est fait par SQLite (index sur `album_id`) plutot qu'en Kotlin : sur 5 000
     * elements, cela evite un tri sur le thread principal a chaque emission.
     */
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

    /**
     * `UPSERT` et non `DELETE` + `INSERT` : la liste affichee n'est jamais vidée pendant une
     * synchronisation, donc pas de clignotement de l'UI.
     */
    @Upsert
    suspend fun upsertAll(albums: List<AlbumEntity>)
}
