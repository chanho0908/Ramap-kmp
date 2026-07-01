package com.peto.ramap.data.fake

import com.peto.ramap.domain.model.ShopWaitingSystem
import com.peto.ramap.domain.repository.ShopWaitingSystemRepository

class FakeShopWaitingSystemRepository(
    private val result: ShopWaitingSystem? = null,
    private val error: Throwable? = null,
) : ShopWaitingSystemRepository {
    val requestedShopIds = mutableListOf<String>()

    override suspend fun fetchShopWaitingSystem(shopId: String): ShopWaitingSystem? {
        requestedShopIds += shopId
        error?.let { throw it }
        return result
    }
}
