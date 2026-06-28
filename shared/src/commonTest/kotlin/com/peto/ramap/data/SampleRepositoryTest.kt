package com.peto.ramap.data

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleRepositoryTest {
    private val repository = SampleRepository()

    @Test
    fun testFetchData() =
        runTest {
            repository.fetchData().test {
                assertEquals("Loading", awaitItem())
                assertEquals("Success", awaitItem())
                awaitComplete()
            }
        }
}
