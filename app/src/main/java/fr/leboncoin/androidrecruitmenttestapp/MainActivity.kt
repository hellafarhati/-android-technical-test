package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.navigation.AlbumsNavHost
import fr.leboncoin.core.designsystem.theme.AlbumsTheme

/**
 * Activite unique.
 *
 * L'ancienne `DetailsActivity` etait declaree comme LAUNCHER dans le manifeste (deux icones
 * dans le lanceur) et ne recevait aucune donnee. La navigation passe desormais par
 * Navigation Compose, avec des arguments typés.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlbumsTheme {
                AlbumsNavHost()
            }
        }
    }
}
