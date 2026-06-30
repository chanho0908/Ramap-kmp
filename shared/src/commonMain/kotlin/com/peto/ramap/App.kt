package com.peto.ramap

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.peto.ramap.theme.RamapTheme
import com.peto.ramap.ui.map.MapRoute

@Composable
@Preview
fun App() {
    RamapTheme {
        MapRoute()
    }
}
