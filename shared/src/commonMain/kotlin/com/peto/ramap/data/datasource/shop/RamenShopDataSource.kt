package com.peto.ramap.data.datasource.shop

import com.peto.ramap.data.model.RamenShopResponse
import com.peto.ramap.domain.model.MapBounds

interface RamenShopDataSource {
    suspend fun fetchRamenShops(bounds: MapBounds): List<RamenShopResponse>
}
