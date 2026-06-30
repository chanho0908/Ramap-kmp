package com.peto.ramap.data.repository

import com.peto.ramap.data.datasource.waiting.ShopWaitingSystemDataSource
import com.peto.ramap.domain.model.ShopWaitingSystem
import com.peto.ramap.domain.repository.ShopWaitingSystemRepository

class DefaultShopWaitingSystemRepository(
    private val dataSource: ShopWaitingSystemDataSource,
) : ShopWaitingSystemRepository {
    override suspend fun fetchShopWaitingSystem(shopId: String): ShopWaitingSystem? = dataSource.fetchShopWaitingSystem(shopId)?.toDomain()
}
