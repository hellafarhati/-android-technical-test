package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

/**
 * Point d'entree Hilt.
 *
 * Le chargeur d'images partage le meme [OkHttpClient] que Retrofit (un seul pool de connexions
 * et de threads) et dispose d'un cache disque : les vignettes deja vues restent visibles
 * hors ligne.
 */
@HiltAndroidApp
class LbcApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(IMAGE_CACHE_SIZE_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()

    private companion object {
        const val IMAGE_CACHE_SIZE_BYTES = 50L * 1024 * 1024
    }
}
