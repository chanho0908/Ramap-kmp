package com.peto.ramap.data.repository

import com.peto.ramap.data.datasource.shop.RamenShopDataSource
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.model.SearchQuery
import com.peto.ramap.domain.repository.RamenShopRepository

class DefaultRamenShopRepository(
    private val dataSource: RamenShopDataSource,
) : RamenShopRepository {
    override suspend fun fetchRamenShops(bounds: MapBounds): RamenShops =
        RamenShops(
            value =
                dataSource
                    .fetchRamenShops(bounds)
                    .map { it.toDomain() }
                    .associateBy { it.id },
        )

    override suspend fun searchRamenShops(
        query: SearchQuery,
        limit: Int,
    ): RamenShops =
        RamenShops(
            value =
                dataSource
                    .searchRamenShops(query, limit)
                    .map { it.toDomain() }
                    .associateBy { it.id },
        )
}
