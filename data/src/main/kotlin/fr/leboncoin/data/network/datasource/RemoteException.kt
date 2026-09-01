package fr.leboncoin.data.network.datasource

/**
 * Erreurs reseau exposees par la couche network.
 * La couche data traduit les exceptions reseau HttpException ou SerializationException ...en erreurs metier.
 */
sealed class RemoteException(cause: Throwable? = null) : Exception(cause) {
    class Network(cause: Throwable? = null) : RemoteException(cause)
    class Server(val code: Int, cause: Throwable? = null) : RemoteException(cause)
    class Serialization(cause: Throwable? = null) : RemoteException(cause)
    class Unknown(cause: Throwable? = null) : RemoteException(cause)
}
