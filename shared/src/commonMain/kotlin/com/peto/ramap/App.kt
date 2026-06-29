package com.peto.ramap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.peto.ramap.ui.map.MapRoute

@Composable
@Preview
fun App() {
    MaterialTheme {
        MapRoute()
    }
}
