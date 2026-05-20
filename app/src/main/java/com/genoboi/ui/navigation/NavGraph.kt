package com.genoboi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.ui.animais.AnimaisScreen
import com.genoboi.ui.cadastro.CadastroAnimalScreen
import com.genoboi.ui.calendario.CalendarioScreen
import com.genoboi.ui.dashboard.DashboardScreen
import com.genoboi.ui.match.GeneMatchScreen

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Animais      : Screen("animais")
    object Match        : Screen("match")
    object Calendario   : Screen("calendario")
    object CadastroAnimal : Screen("cadastro_animal")
    object AnimalDetalhe : Screen("animal/{animalId}") {
        fun createRoute(id: Long) = "animal/$id"
    }
}

@Composable
fun GenoNavGraph(
    navController: NavHostController,
    repository: AnimalRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Dashboard.route,
        modifier       = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAnimais  = { navController.navigate(Screen.Animais.route) },
                onNavigateToAlertas  = { navController.navigate(Screen.Calendario.route) }
            )
        }

        composable(Screen.Animais.route) {
            AnimaisScreen(
                repository         = repository,
                onCadastrarAnimal  = { navController.navigate(Screen.CadastroAnimal.route) },
                onAnimalClick      = { id -> navController.navigate(Screen.AnimalDetalhe.createRoute(id)) }
            )
        }

        composable(Screen.Match.route) {
            GeneMatchScreen()
        }

        composable(Screen.Calendario.route) {
            CalendarioScreen()
        }

        composable(Screen.CadastroAnimal.route) {
            CadastroAnimalScreen(
                repository = repository,
                onVoltar   = { navController.popBackStack() },
                onSalvo    = { navController.popBackStack() }
            )
        }
    }
}
