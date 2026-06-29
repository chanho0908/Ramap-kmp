package com.peto.ramap.data.datasource.di

import com.peto.ramap.data.datasource.shop.RamenShopDataSource
import com.peto.ramap.data.datasource.shop.RemoteRamenShopDataSource
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

val dataSourceModule =
    module {
        single<RamenShopDataSource> {
            RemoteRamenShopDataSource(get<SupabaseClient>())
        }
    }
