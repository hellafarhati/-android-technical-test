package fr.leboncoin.feature.albums.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.leboncoin.feature.albums.AlbumsRoute
import kotlinx.serialization.Serializable

/**
 * Destination type-safe (Navigation Compose 2.8+) : plus de routes en `String` a concatener,
 * les arguments sont verifies a la compilation.
 */
@Serializable
data object AlbumsDestination

fun NavController.navigateToAlbums() = navigate(AlbumsDestination)

fun NavGraphBuilder.albumsScreen(onAlbumClick: (Int) -> Unit) {
    composable<AlbumsDestination> {
        AlbumsRoute(onAlbumClick = onAlbumClick)
    }
}
