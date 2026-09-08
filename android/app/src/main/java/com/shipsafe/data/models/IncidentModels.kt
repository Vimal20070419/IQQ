package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class IncidentMatchDto(
    @SerializedName("incidentId") val incidentId: String,
    @SerializedName("title") val title: String,
    @SerializedName("service") val service: String,
    @SerializedName("similarityScore") val similarityScore: Float,
    @SerializedName("rootCause") val rootCause: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("resolution") val resolution: String,
    @SerializedName("matchingFactors") val matchingFactors: List<String> = emptyList()
)

data class IncidentDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("rootCause") val rootCause: String,
    @SerializedName("changedComponent") val changedComponent: String,
    @SerializedName("affectedServices") val affectedServices: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("resolution") val resolution: String,
    @SerializedName("deploymentPattern") val deploymentPattern: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("lessonsLearned") val lessonsLearned: String?
)
