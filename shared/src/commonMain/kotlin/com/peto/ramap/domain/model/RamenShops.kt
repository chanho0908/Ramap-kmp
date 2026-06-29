package com.peto.ramap.domain.model

data class RamenShops(
    val value: Map<String, RamenShop>,
) {
    fun filterNotContainShops(renderedShopIds: MutableSet<String>) =
        value.values.filterNot { shop ->
            shop.id in renderedShopIds
        }
}
