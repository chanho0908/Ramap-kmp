package com.peto.ramap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform