package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class DeploymentSummary(
    @SerializedName("id") val id: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("version") val version: String,
    @SerializedName("previousVersion") val previousVersion: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("status") val status: String,
    @SerializedName("commitSha") val commitSha: String,
    @SerializedName("commitMessage") val commitMessage: String,
    @SerializedName("author") val author: String,
    @SerializedName("filesChanged") val filesChanged: Int,
    @SerializedName("linesAdded") val linesAdded: Int,
    @SerializedName("linesDeleted") val linesDeleted: Int,
    @SerializedName("databaseMigration") val databaseMigration: Boolean = false,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("risk") val risk: RiskSummaryDto?,
    @SerializedName("latestHealth") val latestHealth: HealthMetricDto?
)

data class RiskSummaryDto(
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("riskScore") val riskScore: Int,
    @SerializedName("summary") val summary: String,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("affectedServices") val affectedServices: List<String> = emptyList()
)

data class DeploymentDetail(
    @SerializedName("id") val id: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("version") val version: String,
    @SerializedName("previousVersion") val previousVersion: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("status") val status: String,
    @SerializedName("commitSha") val commitSha: String,
    @SerializedName("commitMessage") val commitMessage: String,
    @SerializedName("author") val author: String,
    @SerializedName("repository") val repository: String,
    @SerializedName("branch") val branch: String,
    @SerializedName("filesChanged") val filesChanged: Int,
    @SerializedName("linesAdded") val linesAdded: Int,
    @SerializedName("linesDeleted") val linesDeleted: Int,
    @SerializedName("databaseMigration") val databaseMigration: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("files") val files: List<DeploymentFileDto> = emptyList(),
    @SerializedName("riskAssessment") val riskAssessment: RiskAssessmentDto?,
    @SerializedName("healthMetrics") val healthMetrics: List<HealthMetricDto> = emptyList()
)

data class DeploymentFileDto(
    @SerializedName("id") val id: String,
    @SerializedName("filePath") val filePath: String,
    @SerializedName("changeType") val changeType: String,
    @SerializedName("linesChanged") val linesChanged: Int,
    @SerializedName("isSensitive") val isSensitive: Boolean,
    @SerializedName("category") val category: String
)

data class DecisionRequest(
    @SerializedName("action") val action: String,
    @SerializedName("trafficPct") val trafficPct: Int? = null,
    @SerializedName("reason") val reason: String,
    @SerializedName("deviceModel") val deviceModel: String = "iQOO 12 Pro (Android 14)"
)

data class DecisionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("action") val action: String,
    @SerializedName("deploymentId") val deploymentId: String? = null,
    @SerializedName("fromVersion") val fromVersion: String? = null,
    @SerializedName("toVersion") val toVersion: String? = null,
    @SerializedName("trafficPct") val trafficPct: Int? = null,
    @SerializedName("restoredVersion") val restoredVersion: String? = null,
    @SerializedName("status") val status: String? = null
)
