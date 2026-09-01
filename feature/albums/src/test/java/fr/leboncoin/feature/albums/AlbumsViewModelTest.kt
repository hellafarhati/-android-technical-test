package fr.leboncoin.feature.albums

import androidx.lifecycle.SavedStateHandle
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.data.usecase.ObserveAlbumsUseCase
import fr.leboncoin.data.usecase.RefreshAlbumsUseCase
import fr.leboncoin.data.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.model.DataError
import fr.leboncoin.core.model.RefreshResult
import fr.leboncoin.feature.albums.testing.TestAlbums
import fr.leboncoin.feature.albums.testing.FakeAlbumsRepository
import fr.leboncoin.feature.albums.testing.MainDispatcherRule
import fr.leboncoin.feature.albums.testing.TestDispatcherProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AlbumsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAlbumsRepository(TestAlbums.sample)
    private val dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher)

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = AlbumsViewModel(
        observeAlbums = ObserveAlbumsUseCase(repository, dispatchers),
        refreshAlbums = RefreshAlbumsUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
        savedStateHandle = savedStateHandle,
    )

    /** `stateIn(WhileSubscribed)` n'emet que s'il y a un collecteur : on en branche un. */
    private fun TestScope.observe(viewModel: AlbumsViewModel): Job =
        backgroundScope.launch { viewModel.uiState.collect() }

    @Test
    fun `exposes the grouped albums once the first synchronisation succeeds`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.groups.size)
            assertEquals(4, state.photoCount)
            assertNull(state.error)
            assertEquals(1, repository.refreshCount)
        }

    @Test
    fun `keeps the cached content and surfaces the error when the refresh fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.refreshResult = RefreshResult.Failure(DataError.NETWORK)
            val viewModel = createViewModel()
            observe(viewModel)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(DataError.NETWORK, state.error)
            // Le cache local reste affiche : l'application est utilisable hors ligne.
            assertTrue(state.hasContent)
        }

    @Test
    fun `search filters the list on the debounced query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)
            advanceUntilIdle()

            viewModel.onQueryChange("officia")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("officia", state.query)
            assertEquals(1, state.groups.size)
            assertEquals(listOf(3), state.groups.single().photos.map { it.id })
        }

    @Test
    fun `an unmatched search shows an empty state, not an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)
            viewModel.onQueryChange("zzzz")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.hasContent)
            assertTrue(state.isFiltered)
            assertNull(state.error)
        }

    @Test
    fun `the favorites filter only keeps favorite photos`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)
            advanceUntilIdle()

            viewModel.onFilterChange(AlbumsFilter.FAVORITES)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(AlbumsFilter.FAVORITES, state.filter)
            assertEquals(listOf(3), state.groups.flatMap { group -> group.photos.map { it.id } })
        }

    @Test
    fun `toggling a favorite updates the state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        observe(viewModel)
        advanceUntilIdle()

        val photo = viewModel.uiState.value.groups.first().photos.first()
        assertFalse(photo.isFavorite)

        viewModel.onToggleFavorite(photo)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.groups.first().photos.first()
        assertTrue(updated.isFavorite)
    }

    @Test
    fun `state and search survive the loss of the collector (configuration change)`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val subscription = launch { viewModel.uiState.collect() }
            advanceUntilIdle()
            viewModel.onQueryChange("officia")
            advanceUntilIdle()

            // L'ecran est detruit puis recree : le ViewModel, lui, survit.
            subscription.cancel()
            advanceTimeBy(1_000)
            val newSubscription = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("officia", state.query)
            assertEquals(1, state.groups.size)
            // Aucune requete reseau supplementaire n'a ete declenchee par la rotation.
            assertEquals(1, repository.refreshCount)
            newSubscription.cancel()
        }

    @Test
    fun `restores the query saved before a process death`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val restored = SavedStateHandle(
                mapOf("albums_query" to "officia", "albums_filter" to AlbumsFilter.ALL.name),
            )
            val viewModel = createViewModel(restored)
            observe(viewModel)
            advanceUntilIdle()

            assertEquals("officia", viewModel.uiState.value.query)
            assertEquals(1, viewModel.uiState.value.groups.size)
        }

    @Test
    fun `dismissing the error clears it from the state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.refreshResult = RefreshResult.Failure(DataError.SERVER)
            val viewModel = createViewModel()
            observe(viewModel)
            advanceUntilIdle()
            assertEquals(DataError.SERVER, viewModel.uiState.value.error)

            viewModel.onErrorShown()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
        }
}
