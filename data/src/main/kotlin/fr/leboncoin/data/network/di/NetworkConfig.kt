package fr.leboncoin.data.network.di


data class NetworkConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val isLoggingEnabled: Boolean = false,
) {
    companion object {
        const val DEFAULT_BASE_URL: String = "https://static.leboncoin.fr/"
    }
}
