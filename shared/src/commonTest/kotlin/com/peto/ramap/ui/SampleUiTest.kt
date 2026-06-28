package com.peto.ramap.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

class SampleUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testSampleUi() = runComposeUiTest {
        setContent {
            SampleUi()
        }

        onNodeWithText("Initial").assertExists()
        onNodeWithText("Initial").performClick()
        onNodeWithText("Clicked").assertExists()
    }
}
