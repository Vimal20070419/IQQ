package com.shipsafe.data.repository

import com.shipsafe.data.models.*
import com.shipsafe.data.network.RetrofitClient

class ShipSafeRepository {

    private val api = RetrofitClient.api

    suspend fun getDeployments(): Result<List<DeploymentSummary>> {
        return try {
            val response = api.getDeployments()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeploymentById(id: String): Result<DeploymentDetail> {
        return try {
            val response = api.getDeploymentById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRiskAssessment(id: String): Result<RiskAssessmentDto> {
        return try {
            val response = api.getRiskAssessment(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvidence(id: String): Result<EvidenceDto> {
        return try {
            val response = api.getEvidence(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBlastRadius(id: String): Result<BlastRadiusDto> {
        return try {
            val response = api.getBlastRadius(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFailureChain(id: String): Result<FailureChainDto> {
        return try {
            val response = api.getFailureChain(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHealth(id: String): Result<HealthComparisonDto> {
        return try {
            val response = api.getHealth(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRollbackGuard(id: String): Result<RollbackGuardDto> {
        return try {
            val response = api.getRollbackGuard(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeDecision(id: String, action: String, trafficPct: Int? = null, reason: String): Result<DecisionResponse> {
        return try {
            val req = DecisionRequest(action = action, trafficPct = trafficPct, reason = reason)
            val response = when (action) {
                "APPROVE" -> api.approveDeployment(id, req)
                "CANARY" -> api.canaryDeployment(id, req)
                "HOLD" -> api.holdDeployment(id, req)
                "ROLLBACK" -> api.rollbackDeployment(id, req)
                else -> throw IllegalArgumentException("Unknown action $action")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIncidents(): Result<List<IncidentDetailDto>> {
        return try {
            val response = api.getIncidents()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAuditLogs(): Result<List<AuditLogDto>> {
        return try {
            val response = api.getAuditLogs()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun simulateHealthDegradation(deploymentId: String, degrade: Boolean): Result<Map<String, Any>> {
        return try {
            val body = mapOf("deploymentId" to deploymentId, "degrade" to degrade)
            val response = api.simulateHealth(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetDemo(): Result<Map<String, Any>> {
        return try {
            val response = api.resetDemo()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
