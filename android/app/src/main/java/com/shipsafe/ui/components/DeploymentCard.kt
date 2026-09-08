package com.shipsafe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.data.models.DeploymentSummary
import com.shipsafe.ui.theme.*

@Composable
fun DeploymentCard(
    deployment: DeploymentSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val riskScore = deployment.risk?.riskScore ?: 50
    val riskLevel = deployment.risk?.riskLevel ?: "MEDIUM"

    val riskColor = when {
        riskScore >= 75 -> RiskCrimson
        riskScore >= 45 -> CanaryAmber
        else -> SafeEmerald
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Service Name & Environment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = deployment.serviceName,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${deployment.environment.uppercase()} • ${deployment.version}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                RiskPill(riskLevel = riskLevel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Score and Recommendation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RISK SCORE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$riskScore / 100",
                        color = riskColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RECOMMENDED",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deployment.risk?.recommendation ?: "CANARY",
                        color = CyberCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Assessment Summary
            deployment.risk?.summary?.let { summary ->
                Text(
                    text = summary,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Status and Action Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deployment.filesChanged} files changed • ${deployment.createdAt}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "VIEW RISK →",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
