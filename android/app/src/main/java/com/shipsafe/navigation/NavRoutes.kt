package com.shipsafe.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Home : NavRoutes("home")
    object ActiveDeployments : NavRoutes("active_deployments")
    object DeploymentDetail : NavRoutes("deployment_detail/{id}") {
        fun createRoute(id: String) = "deployment_detail/$id"
    }
    object RiskDecisionHero : NavRoutes("risk_decision/{id}") {
        fun createRoute(id: String) = "risk_decision/$id"
    }
    object BlastRadius : NavRoutes("blast_radius/{id}") {
        fun createRoute(id: String) = "blast_radius/$id"
    }
    object FailureChain : NavRoutes("failure_chain/{id}") {
        fun createRoute(id: String) = "failure_chain/$id"
    }
    object HistoricalIncidents : NavRoutes("historical_incidents")
    object StrategySimulator : NavRoutes("strategy_simulator/{id}") {
        fun createRoute(id: String) = "strategy_simulator/$id"
    }
    object HealthMonitoring : NavRoutes("health_monitoring/{id}") {
        fun createRoute(id: String) = "health_monitoring/$id"
    }
    object RollbackGuard : NavRoutes("rollback_guard/{id}") {
        fun createRoute(id: String) = "rollback_guard/$id"
    }
    object Timeline : NavRoutes("timeline/{id}") {
        fun createRoute(id: String) = "timeline/$id"
    }
    object AuditHistory : NavRoutes("audit_history")
    object Settings : NavRoutes("settings")
    object RedLightMode : NavRoutes("red_light_mode/{id}") {
        fun createRoute(id: String) = "red_light_mode/$id"
    }
}
