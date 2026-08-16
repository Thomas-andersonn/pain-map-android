package com.example.painmap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.painmap.ui.screens.MainDashboardScreen
import com.example.painmap.ui.screens.painmap.PainMapViewModel

@Composable
fun PainMapNavHost(
    viewModel: PainMapViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = PainMapRoute.Dashboard.route,
        modifier = modifier
    ) {
        composable(PainMapRoute.Dashboard.route) {
            MainDashboardScreen(
                onStartAssessment = {
                    navController.navigate(PainMapRoute.BodyMap.route)
                }
            )
        }

        composable(PainMapRoute.BodyMap.route) {
            // Placeholder destination wired for TASK-005 full 3D body map screen
            MainDashboardScreen(
                onStartAssessment = {
                    navController.navigate(PainMapRoute.TriageResult.route)
                }
            )
        }

        composable(PainMapRoute.TriageResult.route) {
            // Placeholder destination wired for TASK-007 AI triage report screen
            MainDashboardScreen(
                onStartAssessment = {
                    navController.navigate(PainMapRoute.Dashboard.route)
                }
            )
        }
    }
}
