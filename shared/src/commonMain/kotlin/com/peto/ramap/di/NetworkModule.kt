package com.peto.ramap.di

import com.peto.ramap.network.supabaseClient
import org.koin.dsl.module

val networkModule =
    module {
        single { supabaseClient }
    }
