package fr.leboncoin.feature.albums

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
import fr.leboncoin.core.designsystem.component.AlbumThumbnail
import fr.leboncoin.core.designsystem.component.FavoriteButton

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
             AlbumThumbnail(
                imageUrl = album.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )

            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )

            FavoriteButton(
                isFavorite = album.isFavorite,
                onClick = onToggleFavorite,
            )
        }
    }
}
