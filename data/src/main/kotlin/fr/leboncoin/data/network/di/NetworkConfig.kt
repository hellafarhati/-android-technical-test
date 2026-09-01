package fr.leboncoin.data.network.di

/**
 * Configuration fournie par le module applicatif.
 *
 * `BuildConfig.DEBUG` d'un module bibliotheque ne reflete pas le type de build de l'app :
 * c'est donc `:app` qui decide s'il faut logger, pas the network layer.
 */
data class NetworkConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val isLoggingEnabled: Boolean = false,
) {
    companion object {
        const val DEFAULT_BASE_URL: String = "https://static.leboncoin.fr/"
    }
}
