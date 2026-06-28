package com.peto.ramap.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SampleUi() {
    var text by remember { mutableStateOf("Initial") }

    Button(onClick = { text = "Clicked" }) {
        Text(text)
    }
}
