package fr.leboncoin.androidrecruitmenttestapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.androidrecruitmenttestapp.BuildConfig
import fr.leboncoin.data.network.di.NetworkConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {


    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(
        baseUrl = NetworkConfig.DEFAULT_BASE_URL,
        isLoggingEnabled = BuildConfig.DEBUG,
    )
}
