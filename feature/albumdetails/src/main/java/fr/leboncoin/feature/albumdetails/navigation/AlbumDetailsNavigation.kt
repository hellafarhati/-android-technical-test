package fr.leboncoin.feature.albumdetails.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.leboncoin.feature.albumdetails.AlbumDetailsRoute
import kotlinx.serialization.Serializable

/**
 * Le nom de la propriete ([photoId]) devient la cle de l'argument dans le `SavedStateHandle`,
 * cf. `AlbumDetailsViewModel.ARG_PHOTO_ID`.
 */
@Serializable
data class AlbumDetailsDestination(val photoId: Int)

fun NavController.navigateToAlbumDetails(photoId: Int) =
    navigate(AlbumDetailsDestination(photoId = photoId))

fun NavGraphBuilder.albumDetailsScreen(onBackClick: () -> Unit) {
    composable<AlbumDetailsDestination> {
        AlbumDetailsRoute(onBackClick = onBackClick)
    }
}
