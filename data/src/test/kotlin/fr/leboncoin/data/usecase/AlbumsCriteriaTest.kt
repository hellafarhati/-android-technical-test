package fr.leboncoin.data.usecase

import fr.leboncoin.data.usecase.AlbumsCriteria
import fr.leboncoin.data.usecase.AlbumsFilter
import fr.leboncoin.core.model.Album
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La logique de filtrage/regroupement est une fonction pure : elle se teste sans coroutine,
 * sans Android et sans base de donnees. C'est l'interet de l'avoir sortie du ViewModel.
 */
class AlbumsCriteriaTest {

    private val albums = listOf(
        album(id = 1, albumId = 1, title = "Sunset over Nantes"),
        album(id = 2, albumId = 1, title = "Blue hour", isFavorite = true),
        album(id = 3, albumId = 2, title = "sunset in Lisbon"),
    )

    @Test
    fun `groups photos by album and keeps them ordered`() {
        val groups = albums.applyCriteria(AlbumsCriteria())

        assertEquals(listOf(1, 2), groups.map { it.albumId })
        assertEquals(2, groups.first().size)
    }

    @Test
    fun `search is case insensitive and trimmed`() {
        val groups = albums.applyCriteria(AlbumsCriteria(query = "  SUNSET "))

        assertEquals(listOf(1, 2), groups.map { it.albumId })
        assertEquals(listOf(1, 3), groups.flatMap { group -> group.photos.map { it.id } })
    }

    @Test
    fun `favorites filter drops albums without any favorite`() {
        val groups = albums.applyCriteria(AlbumsCriteria(filter = AlbumsFilter.FAVORITES))

        assertEquals(1, groups.size)
        assertEquals(listOf(2), groups.single().photos.map { it.id })
    }

    @Test
    fun `combining search and favorites can legitimately return nothing`() {
        val groups = albums.applyCriteria(
            AlbumsCriteria(query = "sunset", filter = AlbumsFilter.FAVORITES),
        )

        assertTrue(groups.isEmpty())
    }

    private fun album(id: Int, albumId: Int, title: String, isFavorite: Boolean = false) = Album(
        id = id,
        albumId = albumId,
        title = title,
        imageUrl = "https://example.org/$id",
        thumbnailUrl = "https://example.org/$id/thumb",
        isFavorite = isFavorite,
    )
}
