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
import com.example.painmap.ui.screens.painmap.PainMapScreen
import com.example.painmap.ui.screens.painmap.PainMapUiAction
import com.example.painmap.ui.screens.painmap.PainMapViewModel
import com.example.painmap.ui.screens.triage.TriageResultScreen

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
                sessionsList = uiState.sessionsList,
                onStartAssessment = {
                    navController.navigate(PainMapRoute.BodyMap.route)
                },
                onSelectSession = { sessionId ->
                    viewModel.onAction(
                        PainMapUiAction.LoadSession(
                            sessionId = sessionId,
                            onLoaded = {
                                navController.navigate(PainMapRoute.TriageResult.route)
                            }
                        )
                    )
                }
            )
        }

        composable(PainMapRoute.BodyMap.route) {
            PainMapScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTriage = {
                    navController.navigate(PainMapRoute.TriageResult.route)
                }
            )
        }

        composable(PainMapRoute.TriageResult.route) {
            TriageResultScreen(
                report = uiState.latestTriageReport,
                chatHistory = uiState.chatHistory,
                isAskingFollowUp = uiState.isAskingFollowUp,
                onSendFollowUp = { question ->
                    viewModel.onAction(PainMapUiAction.SendFollowUpQuestion(question))
                },
                onNavigateBackToMap = {
                    navController.navigate(PainMapRoute.BodyMap.route) {
                        popUpTo(PainMapRoute.BodyMap.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(PainMapRoute.Dashboard.route) {
                        popUpTo(PainMapRoute.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
