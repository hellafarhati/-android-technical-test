package fr.leboncoin.feature.albumdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.core.designsystem.component.AlbumThumbnail
import fr.leboncoin.core.designsystem.component.EmptyState
import fr.leboncoin.core.designsystem.component.FavoriteButton
import fr.leboncoin.core.designsystem.component.LoadingState
import fr.leboncoin.core.R as DesignSystemR

@Composable
fun AlbumDetailsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumDetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSparkApi::class)
@Composable
internal fun AlbumDetailsScreen(
    uiState: AlbumDetailsUiState,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.ic_arrow_back),
                            contentDescription = stringResource(DesignSystemR.string.ds_back),
                        )
                    }
                },
                actions = {
                    uiState.album?.let { album ->
                        FavoriteButton(
                            isFavorite = album.isFavorite,
                            onClick = onToggleFavorite,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val album = uiState.album
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))

            album == null -> EmptyState(
                message = stringResource(R.string.details_not_found),
                modifier = Modifier.padding(innerPadding),
            )

            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AlbumThumbnail(
                    imageUrl = album.imageUrl,
                    contentDescription = album.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit,
                )

                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipTinted(text = stringResource(R.string.details_album, album.albumId))
                    ChipTinted(text = stringResource(R.string.details_photo, album.id))
                }
            }
        }
    }
}
