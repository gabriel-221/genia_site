package com.genoboi.data.remote

import android.content.Context
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth

object SupabaseConfig {

    private const val URL      = "https://dlisrdfajsppsquakwlu.supabase.co"
    private const val ANON_KEY = "sb_publishable_NfeQH4fdJO1bgyxg4PSJgg_UPdHnt7L"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = URL,
            supabaseKey = ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

    // ── Produtor ID ──────────────────────────────────────────────────────────
    // O app mantém um produtorId local (UUID) para associar os animais ao
    // registro genia_produtor no Supabase.  É gerado na primeira execução e
    // gravado em SharedPreferences.

    private const val PREFS    = "genoboi_remote_prefs"
    private const val KEY_PROD = "produtor_id"

    fun getProdutorId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PROD, null)

    fun saveProdutorId(context: Context, id: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PROD, id).apply()
}
