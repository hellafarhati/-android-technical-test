package fr.leboncoin.feature.albums

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import fr.leboncoin.core.designsystem.component.EmptyState
import fr.leboncoin.core.designsystem.component.ErrorState
import fr.leboncoin.core.designsystem.component.LoadingState
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.core.model.DataError
import fr.leboncoin.core.R as DesignSystemR

@Composable
fun AlbumsRoute(
    onAlbumClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumsScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onAlbumClick = onAlbumClick,
        onRetry = viewModel::refresh,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun AlbumsScreen(
    uiState: AlbumsUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AlbumsFilter) -> Unit,
    onToggleFavorite: (AlbumUi) -> Unit,
    onAlbumClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    //on affiche une snackbar en cas d'erreur et on continue a servir le cache.

    val errorMessage = uiState.error?.let { stringResource(it.messageRes()) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && uiState.hasContent) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.albums_title)) },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.albums_refresh),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AlbumsFilters(
                query = uiState.query,
                filter = uiState.filter,
                onQueryChange = onQueryChange,
                onFilterChange = onFilterChange,
            )

            val blockingError = uiState.error.takeIf { !uiState.hasContent && !uiState.isFiltered }

            when {
                uiState.isLoading -> LoadingState()

                blockingError != null -> ErrorState(
                    message = stringResource(blockingError.messageRes()),
                    onRetry = onRetry,
                )

                !uiState.hasContent -> EmptyState(message = stringResource(uiState.emptyMessageRes()))

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.groups.forEach { group ->
                        stickyHeader(key = "header-${group.albumId}", contentType = "header") {
                            AlbumGroupHeader(group)
                        }
                        items(
                            items = group.photos,
                            // Cle stable : Compose reutilise les items au lieu de tout recomposer.
                            key = { photo -> photo.id },
                            contentType = { "photo" },
                        ) { photo ->
                            AlbumRow(
                                album = photo,
                                onClick = { onAlbumClick(photo.id) },
                                onToggleFavorite = { onToggleFavorite(photo) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumsFilters(
    query: String,
    filter: AlbumsFilter,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AlbumsFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.albums_search_hint)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filter == AlbumsFilter.ALL,
                onClick = { onFilterChange(AlbumsFilter.ALL) },
                label = { Text(stringResource(R.string.albums_filter_all)) },
            )
            FilterChip(
                selected = filter == AlbumsFilter.FAVORITES,
                onClick = { onFilterChange(AlbumsFilter.FAVORITES) },
                label = { Text(stringResource(R.string.albums_filter_favorites)) },
            )
        }
    }
}

@Composable
private fun AlbumGroupHeader(group: AlbumGroupUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = stringResource(R.string.albums_group_header, group.albumId, group.photos.size),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

internal fun DataError.messageRes(): Int = when (this) {
    DataError.NETWORK -> R.string.error_network
    DataError.SERVER -> R.string.error_server
    DataError.PARSING -> R.string.error_parsing
    DataError.EMPTY -> R.string.error_empty
    DataError.UNKNOWN -> R.string.error_unknown
}

private fun AlbumsUiState.emptyMessageRes(): Int = when {
    filter == AlbumsFilter.FAVORITES && query.isBlank() -> R.string.albums_empty_favorites
    isFiltered -> R.string.albums_empty_search
    else -> R.string.albums_empty
}
