package com.peto.ramap.data.repository.di

import com.peto.ramap.data.datasource.shop.RamenShopDataSource
import com.peto.ramap.data.repository.DefaultRamenShopRepository
import com.peto.ramap.domain.repository.RamenShopRepository
import org.koin.dsl.module

val repositoryModule =
    module {
        single<RamenShopRepository> {
            DefaultRamenShopRepository(get<RamenShopDataSource>())
        }
    }
