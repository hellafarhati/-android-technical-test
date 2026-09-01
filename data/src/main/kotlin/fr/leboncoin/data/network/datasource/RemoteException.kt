package fr.leboncoin.data.network.datasource

/**
 * Erreurs reseau exposees par la couche network.
 *
 * Objectif : ne pas laisser fuiter `retrofit2.HttpException` ou `SerializationException`
 * dans the `data` module et au-dela. La couche data traduit ces types en erreurs metier.
 */
sealed class RemoteException(cause: Throwable? = null) : Exception(cause) {
    class Network(cause: Throwable? = null) : RemoteException(cause)
    class Server(val code: Int, cause: Throwable? = null) : RemoteException(cause)
    class Serialization(cause: Throwable? = null) : RemoteException(cause)
    class Unknown(cause: Throwable? = null) : RemoteException(cause)
}
