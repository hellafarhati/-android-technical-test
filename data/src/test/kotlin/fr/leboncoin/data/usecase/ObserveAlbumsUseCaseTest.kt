package fr.leboncoin.data.usecase

import app.cash.turbine.test
import fr.leboncoin.core.dispatcher.DispatcherProvider
import fr.leboncoin.data.usecase.AlbumsCriteria
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.data.repository.AlbumsRepository
import fr.leboncoin.core.model.Album
import fr.leboncoin.core.model.RefreshResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAlbumsUseCaseTest {

    private val repository = InMemoryAlbumsRepository(
        listOf(
            Album(1, 1, "Sunset", "u1", "t1"),
            Album(2, 2, "Blue hour", "u2", "t2"),
        ),
    )

    @Test
    fun `re-emits when the criteria change`() = runTest {
        val useCase = ObserveAlbumsUseCase(repository, UnconfinedDispatchers(testScheduler))
        val criteria = MutableStateFlow(AlbumsCriteria())

        useCase(criteria).test {
            assertEquals(2, awaitItem().size)

            criteria.value = AlbumsCriteria(query = "sunset")
            assertEquals(listOf(1), awaitItem().map { it.albumId })

            criteria.value = AlbumsCriteria(filter = AlbumsFilter.FAVORITES)
            assertEquals(0, awaitItem().size)
        }
    }

    @Test
    fun `re-emits when the local data changes`() = runTest {
        val useCase = ObserveAlbumsUseCase(repository, UnconfinedDispatchers(testScheduler))
        val criteria = MutableStateFlow(AlbumsCriteria())

        useCase(criteria).test {
            assertEquals(2, awaitItem().size)

            repository.emit(emptyList())
            assertEquals(0, awaitItem().size)
        }
    }

    private class UnconfinedDispatchers(scheduler: TestCoroutineScheduler) : DispatcherProvider {
        private val dispatcher = UnconfinedTestDispatcher(scheduler)
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private class InMemoryAlbumsRepository(initial: List<Album>) : AlbumsRepository {
        private val albums = MutableStateFlow(initial)

        override fun observeAlbums(): Flow<List<Album>> = albums
        override fun observeAlbum(id: Int): Flow<Album?> =
            albums.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun refresh(): RefreshResult = RefreshResult.Success

        override suspend fun setFavorite(photoId: Int, isFavorite: Boolean) = Unit

        fun emit(newAlbums: List<Album>) {
            albums.value = newAlbums
        }
    }
}
