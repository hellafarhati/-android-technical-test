package fr.leboncoin.data.mapper

import fr.leboncoin.data.database.entity.AlbumEntity
import fr.leboncoin.data.database.entity.PopulatedAlbum
import fr.leboncoin.core.model.Album
import fr.leboncoin.data.network.model.AlbumDto

internal fun AlbumDto.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    albumId = albumId,
    title = title.trim(),
    url = url,
    thumbnailUrl = thumbnailUrl,
)

internal fun PopulatedAlbum.toDomain(): Album = Album(
    id = album.id,
    albumId = album.albumId,
    title = album.title,
    imageUrl = album.url,
    thumbnailUrl = album.thumbnailUrl,
    isFavorite = isFavorite,
)
