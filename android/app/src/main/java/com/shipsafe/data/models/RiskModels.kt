package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class RiskAssessmentDto(
    @SerializedName("deploymentId") val deploymentId: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("version") val version: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("riskScore") val riskScore: Int,
    @SerializedName("summary") val summary: String,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("isAiGenerated") val isAiGenerated: Boolean = true,
    @SerializedName("factors") val factors: List<RiskFactorDto> = emptyList(),
    @SerializedName("evidence") val evidence: List<String> = emptyList(),
    @SerializedName("affectedServices") val affectedServices: List<String> = emptyList(),
    @SerializedName("failureChain") val failureChain: List<FailureChainStepDto> = emptyList(),
    @SerializedName("similarIncidents") val similarIncidents: List<IncidentMatchDto> = emptyList()
)

data class RiskFactorDto(
    @SerializedName("factor") val factor: String,
    @SerializedName("points") val points: Int,
    @SerializedName("category") val category: String,
    @SerializedName("evidence") val evidence: String
)

data class EvidenceDto(
    @SerializedName("deploymentId") val deploymentId: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("version") val version: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("filesChangedCount") val filesChangedCount: Int,
    @SerializedName("linesChangedCount") val linesChangedCount: Int,
    @SerializedName("databaseMigration") val databaseMigration: Boolean,
    @SerializedName("changedFiles") val changedFiles: List<DeploymentFileDto> = emptyList(),
    @SerializedName("evidence") val evidence: List<String> = emptyList()
)
