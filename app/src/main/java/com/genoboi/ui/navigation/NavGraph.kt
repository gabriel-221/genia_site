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
import com.genoboi.ui.copilot.AiCopilotScreen
import com.genoboi.ui.dashboard.DashboardScreen
import com.genoboi.ui.match.GeneMatchScreen

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Animais      : Screen("animais")
    object Match        : Screen("match")
    object Calendario   : Screen("calendario")
    object Copilot      : Screen("copilot")
    object Relatorios   : Screen("relatorios")
    object CadastroAnimal : Screen("cadastro_animal?animalId={animalId}") {
        fun createRoute(animalId: Long? = null) = if (animalId != null) "cadastro_animal?animalId=$animalId" else "cadastro_animal"
    }
    object AnimalDetalhe : Screen("animal/{animalId}") {
        fun createRoute(id: Long) = "animal/$id"
    }
    object SelecaoMatriz : Screen("selecao_matriz")
    object SelecaoMacho : Screen("selecao_macho/{matrizId}") {
        fun createRoute(matrizId: Long) = "selecao_macho/$matrizId"
    }
    object ResultadoSimulacao : Screen("resultado_simulacao/{matrizId}/{machoId}") {
        fun createRoute(matrizId: Long, machoId: Long) = "resultado_simulacao/$matrizId/$machoId"
    }
}

@Composable
fun GenoNavGraph(
    navController: NavHostController,
    repository: AnimalRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Dashboard.route,
        modifier         = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAnimais    = { navController.navigate(Screen.Animais.route) },
                onNavigateToAlertas    = { navController.navigate(Screen.Calendario.route) },
                onNavigateToRelatorios = { navController.navigate(Screen.Relatorios.route) }
            )
        }

        composable(Screen.Animais.route) {
            AnimaisScreen(
                repository          = repository,
                onCadastrarAnimal   = { navController.navigate(Screen.CadastroAnimal.createRoute()) },
                onAnimalClick       = { id -> navController.navigate(Screen.AnimalDetalhe.createRoute(id)) },
                onSimularCruzamento = { navController.navigate(Screen.SelecaoMatriz.route) }
            )
        }

        composable(Screen.SelecaoMatriz.route) {
            com.genoboi.ui.animais.SelecaoMatrizScreen(
                repository          = repository,
                onMatrizSelecionada = { id -> navController.navigate(Screen.SelecaoMacho.createRoute(id)) },
                onVoltar            = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.SelecaoMacho.route,
            arguments = listOf(navArgument("matrizId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matrizId = backStackEntry.arguments?.getLong("matrizId") ?: 0L
            com.genoboi.ui.animais.SelecaoMachoScreen(
                matrizId          = matrizId,
                repository        = repository,
                onMachoSelecionado = { mId, rId ->
                    navController.navigate(Screen.ResultadoSimulacao.createRoute(mId, rId))
                },
                onVoltar = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.ResultadoSimulacao.route,
            arguments = listOf(
                navArgument("matrizId") { type = NavType.LongType },
                navArgument("machoId")  { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val matrizId = backStackEntry.arguments?.getLong("matrizId") ?: 0L
            val machoId  = backStackEntry.arguments?.getLong("machoId")  ?: 0L
            com.genoboi.ui.animais.ResultadoSimulacaoScreen(
                matrizId  = matrizId,
                machoId   = machoId,
                repository = repository,
                onFinalizar = { navController.popBackStack(Screen.Animais.route, inclusive = false) }
            )
        }

        composable(
            route     = Screen.AnimalDetalhe.route,
            arguments = listOf(navArgument("animalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getLong("animalId") ?: 0L
            com.genoboi.ui.animais.AnimalDetalheScreen(
                animalId   = animalId,
                repository = repository,
                onVoltar   = { navController.popBackStack() },
                onEditar   = { id -> navController.navigate(Screen.CadastroAnimal.createRoute(id)) }
            )
        }

        composable(Screen.Match.route) {
            GeneMatchScreen(repository)
        }

        composable(Screen.Copilot.route) {
            AiCopilotScreen(repository)
        }

        composable(Screen.Calendario.route) {
            CalendarioScreen()
        }

        composable(Screen.Relatorios.route) {
            com.genoboi.ui.dashboard.RelatoriosScreen(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.CadastroAnimal.route,
            arguments = listOf(navArgument("animalId") {
                type         = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getLong("animalId") ?: -1L
            CadastroAnimalScreen(
                animalId   = if (animalId == -1L) null else animalId,
                repository = repository,
                onVoltar   = { navController.popBackStack() },
                onSalvo    = { navController.popBackStack() }
            )
        }
    }
}
