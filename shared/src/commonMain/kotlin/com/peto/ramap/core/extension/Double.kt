package com.peto.ramap.core.extension

fun Double.toFixedOneDecimal(): String = ((this * 10).toInt() / 10.0).toString()
