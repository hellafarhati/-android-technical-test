package fr.leboncoin.data.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.data.database.AlbumsDatabase
import fr.leboncoin.data.database.dao.AlbumsDao
import fr.leboncoin.data.database.dao.FavoritesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlbumsDatabase =
        Room.databaseBuilder(context, AlbumsDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideAlbumsDao(database: AlbumsDatabase): AlbumsDao = database.albumsDao()

    @Provides
    fun provideFavoritesDao(database: AlbumsDatabase): FavoritesDao = database.favoritesDao()
}

private const val DATABASE_NAME = "albums.db"
