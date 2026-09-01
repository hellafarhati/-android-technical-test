package fr.leboncoin.data.network.api

import fr.leboncoin.data.network.model.AlbumDto
import retrofit2.http.GET

internal interface AlbumsApiService {

    @GET("img/shared/technical-test.json")
    suspend fun getAlbums(): List<AlbumDto>
}
