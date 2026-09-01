package fr.leboncoin.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.core.dispatcher.DefaultDispatcherProvider
import fr.leboncoin.core.dispatcher.DispatcherProvider
import fr.leboncoin.data.repository.AlbumsRepositoryImpl
import fr.leboncoin.data.repository.AlbumsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAlbumsRepository(impl: AlbumsRepositoryImpl): AlbumsRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object DispatchersModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
