package com.shipsafe.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shipsafe.ui.screens.*
import com.shipsafe.viewmodel.*

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
    riskViewModel: RiskDecisionViewModel = viewModel(),
    healthViewModel: HealthViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        // 1. Splash Screen
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(NavRoutes.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen (Safety Center)
        composable(NavRoutes.Home.route) {
            HomeScreen(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                onNavigateToRiskDecision = { id ->
                    navController.navigate(NavRoutes.RiskDecisionHero.createRoute(id))
                },
                onNavigateToActiveDeployments = {
                    navController.navigate(NavRoutes.ActiveDeployments.route)
                },
                onNavigateToIncidents = {
                    navController.navigate(NavRoutes.HistoricalIncidents.route)
                },
                onNavigateToAudit = {
                    navController.navigate(NavRoutes.AuditHistory.route)
                },
                onNavigateToRedLight = { id ->
                    navController.navigate(NavRoutes.RedLightMode.createRoute(id))
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                }
            )
        }

        // 4. Active Deployments Screen
        composable(NavRoutes.ActiveDeployments.route) {
            ActiveDeploymentsScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectDeployment = { id ->
                    navController.navigate(NavRoutes.RiskDecisionHero.createRoute(id))
                }
            )
        }

        // 5. Deployment Detail Screen
        composable(
            route = NavRoutes.DeploymentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            DeploymentDetailScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRiskDecision = {
                    navController.navigate(NavRoutes.RiskDecisionHero.createRoute(id))
                }
            )
        }

        // 6. HERO SCREEN: Risk Decision Surface
        composable(
            route = NavRoutes.RiskDecisionHero.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            RiskDecisionHeroScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBlastRadius = {
                    navController.navigate(NavRoutes.BlastRadius.createRoute(id))
                },
                onNavigateToFailureChain = {
                    navController.navigate(NavRoutes.FailureChain.createRoute(id))
                },
                onNavigateToStrategy = {
                    navController.navigate(NavRoutes.StrategySimulator.createRoute(id))
                },
                onNavigateToHealth = {
                    navController.navigate(NavRoutes.HealthMonitoring.createRoute(id))
                },
                onNavigateToRollbackGuard = {
                    navController.navigate(NavRoutes.RollbackGuard.createRoute(id))
                },
                onNavigateToTimeline = {
                    navController.navigate(NavRoutes.Timeline.createRoute(id))
                }
            )
        }

        // 7. Blast Radius Screen
        composable(
            route = NavRoutes.BlastRadius.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            BlastRadiusScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Failure Chain Screen
        composable(
            route = NavRoutes.FailureChain.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            FailureChainScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Historical Incidents Memory
        composable(NavRoutes.HistoricalIncidents.route) {
            HistoricalIncidentsScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Strategy Simulator Screen
        composable(
            route = NavRoutes.StrategySimulator.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            StrategySimulatorScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onExecuteStrategy = { strategy ->
                    riskViewModel.executeDecision(strategy) {
                        navController.navigate(NavRoutes.HealthMonitoring.createRoute(id))
                    }
                }
            )
        }

        // 11. Health Monitoring Screen (With live degradation simulation)
        composable(
            route = NavRoutes.HealthMonitoring.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            HealthMonitoringScreen(
                deploymentId = id,
                viewModel = healthViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRollbackGuard = {
                    navController.navigate(NavRoutes.RollbackGuard.createRoute(id))
                },
                onNavigateToAudit = {
                    navController.navigate(NavRoutes.AuditHistory.route)
                }
            )
        }

        // 12. Rollback Guard Screen
        composable(
            route = NavRoutes.RollbackGuard.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            RollbackGuardScreen(
                deploymentId = id,
                viewModel = healthViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRollbackComplete = {
                    navController.navigate(NavRoutes.AuditHistory.route) {
                        popUpTo(NavRoutes.Home.route)
                    }
                }
            )
        }

        // 13. Timeline Screen
        composable(
            route = NavRoutes.Timeline.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            TimelineScreen(
                deploymentId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 14. Audit History Screen
        composable(NavRoutes.AuditHistory.route) {
            AuditHistoryScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. Settings Screen
        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // 16. Red Light Emergency Mode
        composable(
            route = NavRoutes.RedLightMode.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "dep-checkout-v284"
            RedLightModeScreen(
                deploymentId = id,
                viewModel = riskViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHealth = {
                    navController.navigate(NavRoutes.HealthMonitoring.createRoute(id))
                }
            )
        }
    }
}
