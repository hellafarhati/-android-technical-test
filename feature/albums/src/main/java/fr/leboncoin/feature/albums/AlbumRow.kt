package fr.leboncoin.feature.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.core.designsystem.component.AlbumThumbnail
import fr.leboncoin.core.designsystem.component.FavoriteButton

/**
 * Ligne de la liste.
 *
 * Elle ne recoit que des donnees immuables et des lambdas : aucune reference au ViewModel,
 * donc pas de fuite de contexte et une previsualisation possible.
 */
@OptIn(ExperimentalSparkApi::class)
@Composable
internal fun AlbumRow(
    album: AlbumUi,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Le modificateur du parent n'est PAS reutilise ici : chaque composant a le sien.
            AlbumThumbnail(
                imageUrl = album.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                ChipTinted(text = "Photo #${album.id}")
            }

            FavoriteButton(
                isFavorite = album.isFavorite,
                onClick = onToggleFavorite,
            )
        }
    }
}
