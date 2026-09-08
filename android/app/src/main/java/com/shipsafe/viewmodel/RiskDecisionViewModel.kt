package com.shipsafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipsafe.data.models.*
import com.shipsafe.data.repository.ShipSafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RiskDecisionUiState(
    val isLoading: Boolean = false,
    val deploymentId: String = "dep-checkout-v284",
    val deployment: DeploymentDetail? = null,
    val riskAssessment: RiskAssessmentDto? = null,
    val blastRadius: BlastRadiusDto? = null,
    val failureChain: FailureChainDto? = null,
    val evidence: EvidenceDto? = null,
    val selectedStrategy: String = "CANARY", // NORMAL, CANARY, HOLD
    val canaryTrafficPct: Int = 10,
    val isDecisionExecuting: Boolean = false,
    val decisionSuccessMessage: String? = null,
    val error: String? = null
)

class RiskDecisionViewModel(
    private val repository: ShipSafeRepository = ShipSafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskDecisionUiState())
    val uiState: StateFlow<RiskDecisionUiState> = _uiState.asStateFlow()

    fun loadDeploymentRisk(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, deploymentId = id, error = null)

            val depResult = repository.getDeploymentById(id)
            val riskResult = repository.getRiskAssessment(id)
            val blastResult = repository.getBlastRadius(id)
            val failureResult = repository.getFailureChain(id)
            val evidenceResult = repository.getEvidence(id)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                deployment = depResult.getOrNull(),
                riskAssessment = riskResult.getOrNull(),
                blastRadius = blastResult.getOrNull(),
                failureChain = failureResult.getOrNull(),
                evidence = evidenceResult.getOrNull()
            )
        }
    }

    fun setStrategy(strategy: String, trafficPct: Int = 10) {
        _uiState.value = _uiState.value.copy(selectedStrategy = strategy, canaryTrafficPct = trafficPct)
    }

    fun executeDecision(action: String, reason: String = "", onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDecisionExecuting = true, error = null)
            val traffic = if (action == "CANARY") _uiState.value.canaryTrafficPct else null
            val customReason = if (reason.isNotBlank()) reason else when (action) {
                "CANARY" -> "AI recommended gradual canary rollout (10% traffic)"
                "APPROVE" -> "Approved standard rollout"
                "HOLD" -> "Release held for SRE review"
                else -> "Rollback executed"
            }

            val result = repository.executeDecision(_uiState.value.deploymentId, action, traffic, customReason)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isDecisionExecuting = false,
                    decisionSuccessMessage = "Successfully executed $action decision!"
                )
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isDecisionExecuting = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to execute $action"
                )
                onComplete(false)
            }
        }
    }
}
