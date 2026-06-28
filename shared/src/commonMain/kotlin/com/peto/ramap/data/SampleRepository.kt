package com.peto.ramap.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SampleRepository {
    fun fetchData(): Flow<String> =
        flow {
            emit("Loading")
            delay(100)
            emit("Success")
        }
}
