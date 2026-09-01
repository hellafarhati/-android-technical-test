package fr.leboncoin.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import fr.leboncoin.core.R

/**
 * Bouton favori accessible : l'etat est expose aux lecteurs d'ecran via `stateDescription`,
 * et la zone tactile respecte les 48dp par defaut de `IconButton`.
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateDescription = stringResource(
        if (isFavorite) R.string.ds_favorite_state_on else R.string.ds_favorite_state_off,
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.semantics { this.stateDescription = stateDescription },
    ) {
        Icon(
            painter = painterResource(
                id = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
            ),
            contentDescription = stringResource(R.string.ds_favorite_action),
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
