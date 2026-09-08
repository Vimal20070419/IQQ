package com.shipsafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipsafe.data.models.HealthComparisonDto
import com.shipsafe.data.models.RollbackGuardDto
import com.shipsafe.data.repository.ShipSafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HealthUiState(
    val isLoading: Boolean = false,
    val deploymentId: String = "dep-checkout-v284",
    val healthData: HealthComparisonDto? = null,
    val rollbackGuard: RollbackGuardDto? = null,
    val isRollbackExecuting: Boolean = false,
    val isRollbackSuccess: Boolean = false,
    val rollbackRestoredVersion: String? = null,
    val error: String? = null
)

class HealthViewModel(
    private val repository: ShipSafeRepository = ShipSafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun loadHealthData(deploymentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, deploymentId = deploymentId, error = null)
            val healthResult = repository.getHealth(deploymentId)
            val guardResult = repository.getRollbackGuard(deploymentId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                healthData = healthResult.getOrNull(),
                rollbackGuard = guardResult.getOrNull()
            )
        }
    }

    fun triggerDegradationSimulation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.simulateHealthDegradation(_uiState.value.deploymentId, true)
            loadHealthData(_uiState.value.deploymentId)
        }
    }

    fun resetHealthSimulation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.simulateHealthDegradation(_uiState.value.deploymentId, false)
            loadHealthData(_uiState.value.deploymentId)
        }
    }

    fun executeRollback(reason: String = "Confirmed rollback after health degradation alert", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRollbackExecuting = true, error = null)
            val result = repository.executeDecision(_uiState.value.deploymentId, "ROLLBACK", reason = reason)
            if (result.isSuccess) {
                val resp = result.getOrNull()
                val targetVer = resp?.toVersion ?: resp?.restoredVersion ?: _uiState.value.rollbackGuard?.targetVersion ?: "v2.8.3"
                _uiState.value = _uiState.value.copy(
                    isRollbackExecuting = false,
                    isRollbackSuccess = true,
                    rollbackRestoredVersion = targetVer
                )
                onComplete()
            } else {
                _uiState.value = _uiState.value.copy(
                    isRollbackExecuting = false,
                    error = result.exceptionOrNull()?.message ?: "Rollback failed"
                )
            }
        }
    }
}
