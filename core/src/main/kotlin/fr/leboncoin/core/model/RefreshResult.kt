package fr.leboncoin.core.model

/**
 * Resultat d'une synchronisation reseau.
 *
 * On modelise l'echec par une enum plutot qu'une exception remontee jusqu'a l'UI :
 * pour éviter d'afficher des exections illisibles IOException, HttpException, ...
 */
sealed interface RefreshResult {
    data object Success : RefreshResult
    data class Failure(val error: DataError) : RefreshResult
}

 enum class DataError {
    /** Pas de connexion, timeout, DNS... */
    NETWORK,

    /** Le serveur a repondu en 4.. / 5... */
    SERVER,

    /** La reponse ne respecte pas le contrat attendu. */
    PARSING,

    /** Le serveur a repondu correctement mais sans donnee exploitable. */
    EMPTY,

    UNKNOWN,
}
