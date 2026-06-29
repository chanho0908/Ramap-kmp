package com.peto.ramap.domain.model

data class RamenShop(
    val id: String,
    val kakaoPlaceId: String?,
    val name: String,
    val address: String,
    val location: Location,
    val kakaoPlaceUrl: String?,
    val phone: String?,
    val businessHours: String?,
    val instagramUrl: String?,
    val kakaoRating: Double?,
    val menuCategoryIds: List<String>,
    val isVisible: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
