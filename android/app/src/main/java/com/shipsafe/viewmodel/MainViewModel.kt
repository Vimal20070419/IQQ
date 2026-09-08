package com.shipsafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipsafe.data.models.AuditLogDto
import com.shipsafe.data.models.DeploymentSummary
import com.shipsafe.data.models.IncidentDetailDto
import com.shipsafe.data.repository.ShipSafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val deployments: List<DeploymentSummary> = emptyList(),
    val activeCount: Int = 0,
    val highRiskCount: Int = 0,
    val pendingDecisionCount: Int = 0,
    val recentIncidents: List<IncidentDetailDto> = emptyList(),
    val auditLogs: List<AuditLogDto> = emptyList(),
    val error: String? = null
)

class MainViewModel(
    private val repository: ShipSafeRepository = ShipSafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val depResult = repository.getDeployments()
            val incResult = repository.getIncidents()
            val auditResult = repository.getAuditLogs()

            val deployments = depResult.getOrDefault(getDefaultDeployments())
            val incidents = incResult.getOrDefault(emptyList())
            val logs = auditResult.getOrDefault(emptyList())

            val active = deployments.filter { it.status == "PENDING" || it.status == "CANARY" }
            val highRisk = deployments.filter { it.risk?.riskScore ?: 0 >= 75 }
            val pending = deployments.filter { it.status == "PENDING" }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                deployments = deployments,
                activeCount = active.size,
                highRiskCount = highRisk.size,
                pendingDecisionCount = pending.size,
                recentIncidents = incidents,
                auditLogs = logs
            )
        }
    }

    private fun getDefaultDeployments(): List<DeploymentSummary> {
        return listOf(
            DeploymentSummary(
                id = "dep-checkout-v284",
                serviceName = "Checkout Service",
                version = "v2.8.4",
                previousVersion = "v2.8.3",
                environment = "production",
                status = "PENDING",
                commitSha = "8f4c2e19d",
                commitMessage = "feat(checkout): optimize payment gateway retry logic and timeout config",
                author = "jordan.dev@shipsafe.io",
                filesChanged = 6,
                linesAdded = 142,
                linesDeleted = 42,
                databaseMigration = false,
                createdAt = "2 min ago",
                risk = com.shipsafe.data.models.RiskSummaryDto(
                    riskLevel = "HIGH",
                    riskScore = 84,
                    summary = "Payment configuration changed and a similar historical incident (INC-017) previously caused checkout outages.",
                    recommendation = "CANARY",
                    affectedServices = listOf("Checkout", "Payment", "Orders", "Inventory")
                ),
                latestHealth = null
            )
        )
    }
}
