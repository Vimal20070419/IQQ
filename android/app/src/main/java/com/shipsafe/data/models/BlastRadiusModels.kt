package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class BlastRadiusDto(
    @SerializedName("deploymentId") val deploymentId: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("directServicesCount") val directServicesCount: Int,
    @SerializedName("downstreamServicesCount") val downstreamServicesCount: Int,
    @SerializedName("totalBlastRadiusCount") val totalBlastRadiusCount: Int,
    @SerializedName("nodes") val nodes: List<BlastRadiusNodeDto> = emptyList(),
    @SerializedName("edges") val edges: List<BlastRadiusEdgeDto> = emptyList()
)

data class BlastRadiusNodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("tier") val tier: Int,
    @SerializedName("type") val type: String, // DIRECT, DOWNSTREAM, CRITICAL_DEPENDENCY
    @SerializedName("impactRisk") val impactRisk: String, // HIGH, MEDIUM, LOW
    @SerializedName("description") val description: String
)

data class BlastRadiusEdgeDto(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("connectionType") val connectionType: String
)
