package fr.leboncoin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import fr.leboncoin.core.R

/**
 * Vignette resiliente : le jeu de donnees de test contient des URLs d'images qui peuvent
 * etre injoignables. On affiche alors un visuel de remplacement au lieu d'un trou blanc.
 */
@Composable
fun AlbumThumbnail(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholder = painterResource(id = R.drawable.ic_image_placeholder)

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = contentScale,
    )
}
