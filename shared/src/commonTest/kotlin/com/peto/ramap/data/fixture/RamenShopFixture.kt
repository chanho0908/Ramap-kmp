package com.peto.ramap.data.fixture

import com.peto.ramap.domain.model.Category
import com.peto.ramap.domain.model.Location
import com.peto.ramap.domain.model.RamenShop

fun ramenShopFixture(): RamenShop =
    RamenShop(
        id = "shop-1",
        kakaoPlaceId = "kakao-shop-1",
        name = "라멘집",
        address = "서울시 마포구 라멘로 1",
        location = Location(lat = 37.551, lng = 126.921),
        kakaoPlaceUrl = "https://place.map.kakao.com/shop-1",
        phone = "02-0000-0000",
        businessHours = "11:00-21:00",
        instagramUrl = "https://instagram.com/ramen_shop",
        kakaoRating = 4.5,
        menuCategories = listOf(Category.SHOYU),
        isVisible = true,
        createdAt = "2026-06-01T00:00:00Z",
        updatedAt = "2026-06-02T00:00:00Z",
    )
