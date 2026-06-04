package com.genoboi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.genoboi.data.local.AppDatabase
import com.genoboi.data.ml.PrenhezModelHelper
import com.genoboi.data.remote.AuthRepository
import com.genoboi.data.remote.SupabaseConfig
import com.genoboi.data.remote.SupabaseRepository
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.ui.auth.CadastroUsuarioScreen
import com.genoboi.ui.auth.LoginScreen
import com.genoboi.ui.components.GenoBottomBar
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.navigation.GenoNavGraph
import com.genoboi.ui.navigation.Screen
import com.genoboi.ui.theme.GenoBOiTheme

class MainActivity : ComponentActivity() {
    private var modelHelper: PrenhezModelHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            modelHelper = PrenhezModelHelper(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Falha ao inicializar modelHelper: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            GenoBOiTheme {
                GenoApp(modelHelper)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        modelHelper?.close()
    }
}

@Composable
fun GenoApp(modelHelper: PrenhezModelHelper?) {
    val context = LocalContext.current
    var isLogado by remember { mutableStateOf(SupabaseConfig.isLogado(context)) }

    if (!isLogado) {
        var showCadastro by remember { mutableStateOf(false) }
        if (showCadastro) {
            CadastroUsuarioScreen(
                onVoltar     = { showCadastro = false },
                onCadastrado = { isLogado = true }
            )
        } else {
            LoginScreen(
                onLoginSucesso = { isLogado = true },
                onCriarConta   = { showCadastro = true }
            )
        }
    } else {
        AppPrincipal(
            modelHelper = modelHelper,
            onLogout    = { isLogado = false }
        )
    }
}

@Composable
private fun AppPrincipal(
    modelHelper: PrenhezModelHelper?,
    onLogout: () -> Unit
) {
    val context    = LocalContext.current
    val db         = remember { AppDatabase.getInstance(context) }
    val remoteRepo = remember { SupabaseRepository(context) }
    val authRepo   = remember { AuthRepository(context, db.produtorDao()) }
    val repository = remember {
        AnimalRepository(db.animalDao(), db.eventoDao(), db.cicloDao(), modelHelper, remoteRepo)
    }

    // Ao abrir o app com sessão já salva, restaura o JWT do Supabase.
    // Sem isso o RLS bloqueia queries porque auth.uid() = null.
    LaunchedEffect(Unit) {
        val email = SupabaseConfig.getEmail(context)
        val produtorId = SupabaseConfig.getProdutorId(context)

        if (email != null && produtorId != null) {
            // Tenta restaurar a sessão online sem precisar da senha —
            // usa o refresh token que o SDK do Supabase mantém em memória.
            // Se o app foi fechado e o token expirou, a sessão não existe mais,
            // mas o sync ainda funciona via anon key para animais com disponivel_match=true.
            try {
                // Verifica se já há sessão ativa
                val sessionAtiva = remoteRepo.temSessaoAtiva()
                if (!sessionAtiva) {
                    Log.w("AppPrincipal", "Sem sessão Supabase — sync usará anon key")
                }
            } catch (e: Exception) {
                Log.w("AppPrincipal", "Erro ao verificar sessão: ${e.message}")
            }
        }

        // Sincroniza animais do Supabase para o banco local
        repository.sincronizarDeRemoto()
        Log.d("AppPrincipal", "Sync inicial concluído")
    }

    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    val showBottomNav = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Animais.route,
        Screen.Match.route,
        Screen.Calendario.route,
        Screen.Copilot.route,
        Screen.AnimalDetalhe.route
    )

    val showTopBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Animais.route,
        Screen.Match.route,
        Screen.Calendario.route
    )

    Scaffold(
        modifier  = Modifier.fillMaxSize(),
        topBar    = {
            if (showTopBar) {
                GenoTopBar(
                    badgeCount = if (currentRoute == Screen.Dashboard.route) 5 else 0
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                GenoBottomBar(
                    currentRoute = currentRoute,
                    onNavigate   = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GenoNavGraph(
            navController = navController,
            repository    = repository,
            modifier      = Modifier.padding(innerPadding)
        )
    }
}
