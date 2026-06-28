package com.peto.ramap

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
