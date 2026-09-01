package fr.leboncoin.data.network.datasource

import fr.leboncoin.data.network.api.AlbumsApiService
import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

interface AlbumsRemoteDataSource {
    /** @throws RemoteException en cas d'echec reseau, serveur ou de parsing. */
    suspend fun fetchAlbums(): List<AlbumDto>
}

internal class DefaultAlbumsRemoteDataSource @Inject constructor(
    private val apiService: AlbumsApiService,
) : AlbumsRemoteDataSource {

    override suspend fun fetchAlbums(): List<AlbumDto> = try {
        apiService.getAlbums()
    } catch (http: HttpException) {
        throw RemoteException.Server(http.code(), http)
    } catch (io: IOException) {
        throw RemoteException.Network(io)
    } catch (serialization: SerializationException) {
        throw RemoteException.Serialization(serialization)
    } catch (throwable: Throwable) {
        throw RemoteException.Unknown(throwable)
    }
}
