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

    private const val PREFS        = "genoboi_remote_prefs"
    private const val KEY_PROD     = "produtor_id"
    private const val KEY_LOGADO   = "is_logged_in"

    fun getProdutorId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PROD, null)

    fun saveProdutorId(context: Context, id: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PROD, id).apply()

    fun clearProdutorId(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_PROD).apply()

    fun isLogado(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOGADO, false)

    fun setLogado(context: Context, logado: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOGADO, logado).apply()
}
