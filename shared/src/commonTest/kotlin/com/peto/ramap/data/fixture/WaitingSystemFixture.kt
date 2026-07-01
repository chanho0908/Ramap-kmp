package com.peto.ramap.data.fixture

import com.peto.ramap.domain.model.ShopWaitingSystem
import com.peto.ramap.domain.model.WaitingProvider

fun waitingSystemFixture(shopId: String): ShopWaitingSystem =
    ShopWaitingSystem(
        id = "waiting-$shopId",
        shopId = shopId,
        provider = WaitingProvider.CATCHTABLE,
        providerUrl = "https://app.catchtable.co.kr/ct/shop/$shopId",
    )
