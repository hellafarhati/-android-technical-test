package fr.leboncoin.feature.albums.testing

import fr.leboncoin.core.model.Album

 object TestAlbums {

    fun album(
        id: Int,
        albumId: Int = 1,
        title: String = "Photo $id",
        isFavorite: Boolean = false,
    ): Album = Album(
        id = id,
        albumId = albumId,
        title = title,
        imageUrl = "https://example.org/full/$id.png",
        thumbnailUrl = "https://example.org/thumb/$id.png",
        isFavorite = isFavorite,
    )

    /** 2 albums de 2 photos, la photo 3 etant en favori. */
    val sample: List<Album> = listOf(
        album(id = 1, albumId = 1, title = "accusamus beatae ad facilis"),
        album(id = 2, albumId = 1, title = "reprehenderit est deserunt"),
        album(id = 3, albumId = 2, title = "officia porro iure quia", isFavorite = true),
        album(id = 4, albumId = 2, title = "culpa odio esse rerum"),
    )
}
