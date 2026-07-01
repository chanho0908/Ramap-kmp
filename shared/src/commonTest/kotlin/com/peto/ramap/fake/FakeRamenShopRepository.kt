package com.peto.ramap.fake

import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.model.SearchQuery
import com.peto.ramap.domain.repository.RamenShopRepository

class FakeRamenShopRepository(
    private val result: RamenShops = RamenShops(emptyMap()),
    private val searchResult: RamenShops = RamenShops(emptyMap()),
) : RamenShopRepository {
    val requestedBoundsHistory = mutableListOf<MapBounds>()
    val requestedSearchQueries = mutableListOf<SearchQuery>()
    val requestedSearchLimits = mutableListOf<Int>()

    override suspend fun fetchRamenShops(bounds: MapBounds): RamenShops {
        requestedBoundsHistory += bounds
        return result
    }

    override suspend fun searchRamenShops(
        query: SearchQuery,
        limit: Int,
    ): RamenShops {
        requestedSearchQueries += query
        requestedSearchLimits += limit
        return searchResult
    }
}
