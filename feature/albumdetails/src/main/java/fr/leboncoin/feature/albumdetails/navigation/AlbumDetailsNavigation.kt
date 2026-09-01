package fr.leboncoin.feature.albumdetails.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.leboncoin.feature.albumdetails.AlbumDetailsRoute
import kotlinx.serialization.Serializable


@Serializable
data class AlbumDetailsDestination(val photoId: Int)

fun NavController.navigateToAlbumDetails(photoId: Int) =
    navigate(AlbumDetailsDestination(photoId = photoId))

fun NavGraphBuilder.albumDetailsScreen(onBackClick: () -> Unit) {
    composable<AlbumDetailsDestination> {
        AlbumDetailsRoute(onBackClick = onBackClick)
    }
}
