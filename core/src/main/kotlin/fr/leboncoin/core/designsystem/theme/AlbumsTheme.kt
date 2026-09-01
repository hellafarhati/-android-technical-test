package fr.leboncoin.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.adevinta.spark.SparkTheme

/**
 * Point d'entree unique du theme.
 *
 * On passe par le design system maison (Spark) plutot que par un theme Material3 ad hoc :
 * couleurs, typographies et composants restent alignes avec le reste des applications
 * leboncoin. Encapsuler l'appel ici permettrait aussi de changer de DS sans toucher aux features.
 */
@Composable
fun AlbumsTheme(content: @Composable () -> Unit) {
    SparkTheme(content = content)
}

/** Espacements utilises par les features, pour eviter les "magic numbers" dans l'UI. */
object AlbumsSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
}
