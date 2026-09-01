package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.leboncoin.feature.albumdetails.navigation.albumDetailsScreen
import fr.leboncoin.feature.albumdetails.navigation.navigateToAlbumDetails
import fr.leboncoin.feature.albums.navigation.AlbumsDestination
import fr.leboncoin.feature.albums.navigation.albumsScreen

/**
 * Graphe de navigation.
 *
 la navigation d'une page a l'aure est assurée par ce graphe
 */
@Composable
fun AlbumsNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AlbumsDestination,
    ) {
        albumsScreen(
            onAlbumClick = { photoId -> navController.navigateToAlbumDetails(photoId) },
        )
        albumDetailsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
