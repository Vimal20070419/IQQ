package com.shipsafe.data.models

import com.google.gson.annotations.SerializedName

data class FailureChainDto(
    @SerializedName("deploymentId") val deploymentId: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("label") val label: String,
    @SerializedName("steps") val steps: List<FailureChainStepDto> = emptyList()
)

data class FailureChainStepDto(
    @SerializedName("step") val step: Int,
    @SerializedName("component") val component: String,
    @SerializedName("state") val state: String,
    @SerializedName("impact") val impact: String,
    @SerializedName("evidence") val evidence: String
)
