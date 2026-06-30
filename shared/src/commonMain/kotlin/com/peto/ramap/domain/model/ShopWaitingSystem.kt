package com.peto.ramap.domain.model

data class ShopWaitingSystem(
    val id: String,
    val shopId: String,
    val provider: WaitingProvider,
    val providerUrl: String?,
)
