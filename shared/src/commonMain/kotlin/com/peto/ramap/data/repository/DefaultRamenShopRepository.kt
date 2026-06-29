package com.peto.ramap.data.repository

import com.peto.ramap.data.datasource.shop.RamenShopDataSource
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.repository.RamenShopRepository

class DefaultRamenShopRepository(
    private val dataSource: RamenShopDataSource,
) : RamenShopRepository {
    override suspend fun fetchRamenShops(bounds: MapBounds): RamenShops {
        val response: List<RamenShop> =
            dataSource.fetchRamenShops(bounds).map { it.toDomain() }
        val result = RamenShops(response)
        return result
    }
}
