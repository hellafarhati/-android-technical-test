package fr.leboncoin.core.model

/**
 * Resultat d'une synchronisation reseau.
 *
 * On modelise l'echec avec un type ferme plutot qu'une exception remontee jusqu'a l'UI :
 * la couche presentation n'a pas a connaitre `IOException`, `HttpException`, etc.
 */
sealed interface RefreshResult {
    data object Success : RefreshResult
    data class Failure(val error: DataError) : RefreshResult
}

/** Erreurs metier exposees a l'UI, traduites en messages localises par la couche presentation. */
enum class DataError {
    /** Pas de connexion, timeout, DNS... */
    NETWORK,

    /** Le serveur a repondu en 4xx / 5xx. */
    SERVER,

    /** La reponse ne respecte pas le contrat attendu. */
    PARSING,

    /** Le serveur a repondu correctement mais sans donnee exploitable. */
    EMPTY,

    UNKNOWN,
}
