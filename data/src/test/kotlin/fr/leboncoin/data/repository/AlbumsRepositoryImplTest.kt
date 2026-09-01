package fr.leboncoin.data.repository

import app.cash.turbine.test
import fr.leboncoin.data.repository.fake.FakeAlbumsRemoteDataSource
import fr.leboncoin.data.repository.fake.FakeAlbumsStore
import fr.leboncoin.data.repository.fake.albumDto
import fr.leboncoin.core.model.DataError
import fr.leboncoin.core.model.RefreshResult
import fr.leboncoin.data.network.datasource.RemoteException
import fr.leboncoin.data.repository.fake.TestDispatcherProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AlbumsRepositoryImplTest {

    private val store = FakeAlbumsStore()
    private val remoteDataSource = FakeAlbumsRemoteDataSource()

    private lateinit var repository: AlbumsRepositoryImpl

    @Before
    fun setUp() {
        repository = AlbumsRepositoryImpl(
            remoteDataSource = remoteDataSource,
            albumsDao = store,
            favoritesDao = store,
            dispatchers = TestDispatcherProvider(),
        )
    }

    @Test
    fun `refresh stores the remote payload in the local database`() = runTest {
        remoteDataSource.result = Result.success(
            listOf(albumDto(id = 2, albumId = 1), albumDto(id = 1, albumId = 1)),
        )

        val result = repository.refresh()

        assertEquals(RefreshResult.Success, result)
        repository.observeAlbums().test {
            val albums = awaitItem()
            assertEquals(listOf(1, 2), albums.map { it.id })
            assertEquals("Photo 1", albums.first().title)
        }
    }

    @Test
    fun `a network failure keeps the cached data and reports a typed error`() = runTest {
        remoteDataSource.result = Result.success(listOf(albumDto(id = 1)))
        repository.refresh()

        remoteDataSource.result = Result.failure(RemoteException.Network(IOException()))
        val result = repository.refresh()

        assertEquals(RefreshResult.Failure(DataError.NETWORK), result)
        repository.observeAlbums().test {
            // Le cache est intact : l'ecran reste utilisable hors ligne.
            assertEquals(1, awaitItem().size)
        }
    }

    @Test
    fun `a server failure is mapped to a server error`() = runTest {
        remoteDataSource.result = Result.failure(RemoteException.Server(code = 503))

        assertEquals(RefreshResult.Failure(DataError.SERVER), repository.refresh())
    }

    @Test
    fun `an unexpected throwable never escapes the repository`() = runTest {
        remoteDataSource.result = Result.failure(IllegalStateException("boom"))

        assertEquals(RefreshResult.Failure(DataError.UNKNOWN), repository.refresh())
    }

    @Test
    fun `an empty payload does not wipe the cache`() = runTest {
        remoteDataSource.result = Result.success(listOf(albumDto(id = 1)))
        repository.refresh()

        remoteDataSource.result = Result.success(emptyList())
        val result = repository.refresh()

        assertEquals(RefreshResult.Failure(DataError.EMPTY), result)
        repository.observeAlbums().test { assertEquals(1, awaitItem().size) }
    }

    @Test
    fun `favorites survive a new synchronisation`() = runTest {
        remoteDataSource.result = Result.success(listOf(albumDto(id = 1, title = "avant")))
        repository.refresh()
        repository.setFavorite(photoId = 1, isFavorite = true)

        remoteDataSource.result = Result.success(listOf(albumDto(id = 1, title = "apres")))
        repository.refresh()

        repository.observeAlbums().test {
            val album = awaitItem().single()
            assertEquals("apres", album.title)
            assertTrue("Le favori doit survivre au refresh", album.isFavorite)
        }
    }

    @Test
    fun `toggling a favorite re-emits the observed album`() = runTest {
        remoteDataSource.result = Result.success(listOf(albumDto(id = 7)))
        repository.refresh()

        repository.observeAlbum(7).test {
            assertEquals(false, awaitItem()?.isFavorite)

            repository.setFavorite(photoId = 7, isFavorite = true)
            assertEquals(true, awaitItem()?.isFavorite)

            repository.setFavorite(photoId = 7, isFavorite = false)
            assertEquals(false, awaitItem()?.isFavorite)
        }
    }

    @Test
    fun `observing an unknown album emits null instead of failing`() = runTest {
        repository.observeAlbum(404).test { assertNull(awaitItem()) }
    }
}
