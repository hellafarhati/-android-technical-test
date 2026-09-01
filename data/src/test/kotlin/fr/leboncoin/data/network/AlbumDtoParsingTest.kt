package fr.leboncoin.data.network

import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Le contrat JSON est la principale source de regression cote reseau :
 * on verrouille le parsing (cles inconnues, valeurs nulles, champs manquants).
 */
class AlbumDtoParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Test
    fun `parses the nominal payload`() {
        val payload = """
            [
              {
                "albumId": 1,
                "id": 1,
                "title": "accusamus beatae ad facilis cum similique qui sunt",
                "url": "https://example.org/600/92c952",
                "thumbnailUrl": "https://example.org/150/92c952"
              }
            ]
        """.trimIndent()

        val albums = json.decodeFromString<List<AlbumDto>>(payload)

        assertEquals(1, albums.size)
        assertEquals(1, albums.first().albumId)
        assertEquals("https://example.org/150/92c952", albums.first().thumbnailUrl)
    }

    @Test
    fun `ignores unknown keys instead of failing`() {
        val payload = """
            [{ "albumId": 1, "id": 1, "title": "t", "url": "u", "thumbnailUrl": "tu", "newField": 42 }]
        """.trimIndent()

        assertEquals(1, json.decodeFromString<List<AlbumDto>>(payload).size)
    }

    @Test
    fun `falls back to defaults when a string field is null or missing`() {
        val payload = """[{ "albumId": 1, "id": 1, "title": null }]"""

        val album = json.decodeFromString<List<AlbumDto>>(payload).single()

        assertEquals("", album.title)
        assertEquals("", album.url)
        assertEquals("", album.thumbnailUrl)
    }
}
