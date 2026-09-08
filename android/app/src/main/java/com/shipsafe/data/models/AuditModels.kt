package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class AuditLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("deploymentId") val deploymentId: String?,
    @SerializedName("userName") val userName: String,
    @SerializedName("userRole") val userRole: String,
    @SerializedName("action") val action: String,
    @SerializedName("details") val details: String,
    @SerializedName("result") val result: String,
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("deviceModel") val deviceModel: String?,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("deployment") val deployment: DeploymentMiniDto?
)

data class DeploymentMiniDto(
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("version") val version: String,
    @SerializedName("environment") val environment: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserProfileDto
)

data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("avatar") val avatar: String?
)
