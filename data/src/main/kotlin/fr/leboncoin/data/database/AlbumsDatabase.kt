package fr.leboncoin.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.leboncoin.data.database.dao.AlbumsDao
import fr.leboncoin.data.database.dao.FavoritesDao
import fr.leboncoin.data.database.entity.AlbumEntity
import fr.leboncoin.data.database.entity.FavoriteEntity

@Database(
    entities = [AlbumEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AlbumsDatabase : RoomDatabase() {
    abstract fun albumsDao(): AlbumsDao
    abstract fun favoritesDao(): FavoritesDao
}
