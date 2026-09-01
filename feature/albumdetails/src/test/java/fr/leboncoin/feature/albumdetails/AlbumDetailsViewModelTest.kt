package fr.leboncoin.feature.albumdetails

import androidx.lifecycle.SavedStateHandle
import fr.leboncoin.data.usecase.ObserveAlbumUseCase
import fr.leboncoin.data.usecase.ToggleFavoriteUseCase
import fr.leboncoin.feature.albumdetails.testing.TestAlbums
import fr.leboncoin.feature.albumdetails.testing.FakeAlbumsRepository
import fr.leboncoin.feature.albumdetails.testing.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AlbumDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAlbumsRepository(TestAlbums.sample)

    private fun createViewModel(photoId: Int?) = AlbumDetailsViewModel(
        observeAlbum = ObserveAlbumUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
        savedStateHandle = SavedStateHandle(
            if (photoId == null) emptyMap() else mapOf("photoId" to photoId),
        ),
    )

    private fun TestScope.observe(viewModel: AlbumDetailsViewModel) =
        backgroundScope.launch { viewModel.uiState.collect() }

    @Test
    fun `exposes the requested photo`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(photoId = 3)
        observe(viewModel)
        advanceUntilIdle()

        val album = viewModel.uiState.value.album
        assertEquals(3, album?.id)
        assertEquals(2, album?.albumId)
        assertTrue(album?.isFavorite == true)
    }

    @Test
    fun `reports a missing photo instead of crashing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(photoId = 404)
            observe(viewModel)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.album)
            assertTrue(viewModel.uiState.value.isNotFound)
        }

    @Test
    fun `toggling the favorite is persisted and reflected in the state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(photoId = 1)
            observe(viewModel)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.album?.isFavorite == true)

            viewModel.onToggleFavorite()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.album?.isFavorite == true)
        }

    @Test
    fun `fails fast when the navigation argument is missing`() {
        assertThrows(IllegalArgumentException::class.java) { createViewModel(photoId = null) }
    }
}
