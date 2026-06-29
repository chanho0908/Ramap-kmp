package com.peto.ramap.core.base

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class SideEffectHolder<SE : SideEffect> {
    private val channel = Channel<SE>(Channel.BUFFERED)
    val flow: Flow<SE> = channel.receiveAsFlow()

    suspend fun emit(effect: SE) {
        channel.send(effect)
    }

    fun tryEmit(effect: SE) {
        channel.trySend(effect)
    }
}
