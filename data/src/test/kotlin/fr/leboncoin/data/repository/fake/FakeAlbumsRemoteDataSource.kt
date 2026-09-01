package fr.leboncoin.data.repository.fake

import fr.leboncoin.data.network.datasource.AlbumsRemoteDataSource
import fr.leboncoin.data.network.model.AlbumDto

class FakeAlbumsRemoteDataSource : AlbumsRemoteDataSource {

    /** Reponse renvoyee par fetchAlbums() ; peut aussi renvoyer une exception */
    var result: Result<List<AlbumDto>> = Result.success(emptyList())

    var callCount: Int = 0
        private set

    override suspend fun fetchAlbums(): List<AlbumDto> {
        callCount++
        return result.getOrThrow()
    }
}

fun albumDto(id: Int, albumId: Int = 1, title: String = "Photo $id"): AlbumDto = AlbumDto(
    id = id,
    albumId = albumId,
    title = title,
    url = "https://example.org/full/$id.png",
    thumbnailUrl = "https://example.org/thumb/$id.png",
)
