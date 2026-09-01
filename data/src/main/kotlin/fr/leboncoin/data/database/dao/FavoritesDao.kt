package fr.leboncoin.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.leboncoin.data.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Upsert
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE photo_id = :photoId")
    suspend fun remove(photoId: Int)

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeCount(): Flow<Int>
}
