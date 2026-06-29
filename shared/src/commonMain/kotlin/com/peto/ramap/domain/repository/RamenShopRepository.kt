package com.peto.ramap.domain.repository

import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops

interface RamenShopRepository {
    suspend fun fetchRamenShops(bounds: MapBounds): RamenShops
}
