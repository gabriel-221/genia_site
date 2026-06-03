package com.genoboi

import android.os.Bundle
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
import com.genoboi.data.remote.SupabaseRepository
import com.genoboi.data.repository.AnimalRepository
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
            android.util.Log.e("MainActivity", "Falha ao inicializar modelHelper: ${e.message}")
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
    val context        = LocalContext.current
    val db             = remember { AppDatabase.getInstance(context) }
    val remoteRepo     = remember { SupabaseRepository(context) }
    val repository     = remember {
        AnimalRepository(db.animalDao(), db.eventoDao(), db.cicloDao(), modelHelper, remoteRepo)
    }

    // Sincroniza do Supabase na abertura do app (em background, sem bloquear a UI)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        repository.sincronizarDeRemoto()
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // Telas que exibem a bottom nav
    val showBottomNav = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Animais.route,
        Screen.Match.route,
        Screen.Calendario.route,
        Screen.AnimalDetalhe.route
    )

    // Telas que exibem a top bar padrão GENIA
    val showTopBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Animais.route,
        Screen.Match.route,
        Screen.Calendario.route
    )

    val topBarTitulo = when (currentRoute) {
        Screen.Animais.route    -> ""   // usa logo GENIA
        Screen.Match.route      -> ""
        Screen.Calendario.route -> ""
        else                    -> ""
    }

    Scaffold(
        modifier   = Modifier.fillMaxSize(),
        topBar     = {
            if (showTopBar) {
                GenoTopBar(
                    badgeCount = if (currentRoute == Screen.Dashboard.route) 5 else 0
                )
            }
        },
        bottomBar  = {
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
