package com.shipsafe.data.network

import com.shipsafe.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ShipSafeApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/demo-users")
    suspend fun getDemoUsers(): Response<List<UserProfileDto>>

    @GET("api/deployments")
    suspend fun getDeployments(): Response<List<DeploymentSummary>>

    @GET("api/deployments/{id}")
    suspend fun getDeploymentById(@Path("id") id: String): Response<DeploymentDetail>

    @GET("api/deployments/{id}/risk")
    suspend fun getRiskAssessment(@Path("id") id: String): Response<RiskAssessmentDto>

    @GET("api/deployments/{id}/evidence")
    suspend fun getEvidence(@Path("id") id: String): Response<EvidenceDto>

    @GET("api/deployments/{id}/blast-radius")
    suspend fun getBlastRadius(@Path("id") id: String): Response<BlastRadiusDto>

    @GET("api/deployments/{id}/failure-chain")
    suspend fun getFailureChain(@Path("id") id: String): Response<FailureChainDto>

    @GET("api/deployments/{id}/health")
    suspend fun getHealth(@Path("id") id: String): Response<HealthComparisonDto>

    @GET("api/deployments/{id}/rollback-guard")
    suspend fun getRollbackGuard(@Path("id") id: String): Response<RollbackGuardDto>

    @POST("api/deployments/{id}/approve")
    suspend fun approveDeployment(
        @Path("id") id: String,
        @Body request: DecisionRequest
    ): Response<DecisionResponse>

    @POST("api/deployments/{id}/canary")
    suspend fun canaryDeployment(
        @Path("id") id: String,
        @Body request: DecisionRequest
    ): Response<DecisionResponse>

    @POST("api/deployments/{id}/hold")
    suspend fun holdDeployment(
        @Path("id") id: String,
        @Body request: DecisionRequest
    ): Response<DecisionResponse>

    @POST("api/deployments/{id}/rollback")
    suspend fun rollbackDeployment(
        @Path("id") id: String,
        @Body request: DecisionRequest
    ): Response<DecisionResponse>

    @GET("api/incidents")
    suspend fun getIncidents(): Response<List<IncidentDetailDto>>

    @GET("api/audit")
    suspend fun getAuditLogs(): Response<List<AuditLogDto>>

    // Demo Scenarios
    @POST("api/demo/deployment")
    suspend fun triggerDemoScenario(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/demo/simulate-health")
    suspend fun simulateHealth(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @POST("api/demo/reset")
    suspend fun resetDemo(): Response<Map<String, Any>>
}
