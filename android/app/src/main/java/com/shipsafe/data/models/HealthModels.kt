package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class HealthComparisonDto(
    @SerializedName("isDegraded") val isDegraded: Boolean,
    @SerializedName("baseline") val baseline: HealthMetricDto,
    @SerializedName("current") val current: HealthMetricDto,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("triggerReason") val triggerReason: String? = null
)

data class HealthMetricDto(
    @SerializedName("errorRate") val errorRate: Float,
    @SerializedName("latencyMs") val latencyMs: Int,
    @SerializedName("http5xxCount") val http5xxCount: Int,
    @SerializedName("cpuPercent") val cpuPercent: Float,
    @SerializedName("requestsPerSec") val requestsPerSec: Int,
    @SerializedName("status") val status: String,
    @SerializedName("recordedAt") val recordedAt: String
)

data class RollbackGuardDto(
    @SerializedName("canRollback") val canRollback: Boolean,
    @SerializedName("currentVersion") val currentVersion: String,
    @SerializedName("targetVersion") val targetVersion: String,
    @SerializedName("targetVerified") val targetVerified: Boolean,
    @SerializedName("hasDatabaseMigration") val hasDatabaseMigration: Boolean,
    @SerializedName("migrationWarning") val migrationWarning: String? = null,
    @SerializedName("rollbackRiskLevel") val rollbackRiskLevel: String,
    @SerializedName("checks") val checks: List<RollbackCheckItemDto> = emptyList()
)

data class RollbackCheckItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("passed") val passed: Boolean,
    @SerializedName("detail") val detail: String
)
