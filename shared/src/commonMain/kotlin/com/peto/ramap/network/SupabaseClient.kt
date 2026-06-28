package com.peto.ramap.network

import com.peto.ramap.shared.RamapConfig
import io.github.jan.tennert.supabase.auth.Auth
import io.github.jan.tennert.supabase.createSupabaseClient
import io.github.jan.tennert.supabase.postgrest.Postgrest
import io.github.jan.tennert.supabase.storage.Storage

val supabaseClient =
    createSupabaseClient(
        supabaseUrl = RamapConfig.SUPABASE_URL,
        supabaseKey = RamapConfig.SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth)
    }
