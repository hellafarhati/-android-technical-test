package fr.leboncoin.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Abstraction des dispatchers des coroutines.
 *
 * On l'injecte plutot que d'appeler `Dispatchers.IO` directement : les tests peuvent alors
 * substituer un `TestDispatcher` et rester deterministes, sans `Thread.sleep` ni attente active.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
