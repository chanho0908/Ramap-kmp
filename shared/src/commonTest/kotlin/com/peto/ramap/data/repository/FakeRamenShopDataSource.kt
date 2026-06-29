package com.peto.ramap.data.repository

import com.peto.ramap.data.datasource.shop.RamenShopDataSource
import com.peto.ramap.data.model.RamenShopResponse
import com.peto.ramap.domain.model.MapBounds

class FakeRamenShopDataSource(
    private val responses: List<RamenShopResponse> = emptyList(),
    private val error: Throwable? = null,
) : RamenShopDataSource {
    var requestedBounds: MapBounds? = null
        private set
    val requestedBoundsHistory = mutableListOf<MapBounds>()

    override suspend fun fetchRamenShops(bounds: MapBounds): List<RamenShopResponse> {
        requestedBounds = bounds
        requestedBoundsHistory += bounds
        error?.let { throw it }
        return responses
    }
}
