package com.peto.ramap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.peto.ramap.ui.KakaoMapView

@Composable
@Preview
fun App() {
    MaterialTheme {
        KakaoMapView(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
