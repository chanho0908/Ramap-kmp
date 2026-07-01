package com.peto.ramap.domain.model

data class RamenShops(
    val value: Map<String, RamenShop>,
) {
    fun filterNotContainShops(renderedShopIds: MutableSet<String>) =
        value.values.filterNot { shop ->
            shop.id in renderedShopIds
        }

    fun filterByCategory(filter: RamenShopFilter): RamenShops {
        if (filter.isEmpty) return this
        return copy(
            value =
                value
                    .filterValues { shop ->
                        shop.menuCategories.any { category ->
                            category in filter.values
                        }
                    },
        )
    }
}
