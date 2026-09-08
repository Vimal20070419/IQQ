package com.shipsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.data.models.HealthComparisonDto
import com.shipsafe.ui.theme.*

@Composable
fun HealthLiveChart(
    health: HealthComparisonDto?,
    modifier: Modifier = Modifier
) {
    val isDegraded = health?.isDegraded == true
    val current = health?.current
    val baseline = health?.baseline

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDegraded) RedLightBackground else SurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDegraded) RiskCrimson else SurfaceBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE DEPLOYMENT TELEMETRY",
                    color = if (isDegraded) RiskCrimson else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isDegraded) "🚨 DEGRADATION DETECTED" else "🟢 STABLE RUNNING",
                    color = if (isDegraded) RiskCrimson else SafeEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid (2x2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Metric 1: Error Rate
                MetricCard(
                    title = "ERROR RATE",
                    value = if (isDegraded) "${baseline?.errorRate ?: 0.8}% → ${current?.errorRate ?: 7.4}%" else "${current?.errorRate ?: 0.8}%",
                    isElevated = isDegraded,
                    color = if (isDegraded) RiskCrimson else SafeEmerald,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Latency
                MetricCard(
                    title = "LATENCY (P99)",
                    value = if (isDegraded) "${baseline?.latencyMs ?: 220}ms → ${current?.latencyMs ?: 780}ms" else "${current?.latencyMs ?: 220}ms",
                    isElevated = isDegraded,
                    color = if (isDegraded) RiskCrimson else SafeEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Metric 3: HTTP 5xx Count
                MetricCard(
                    title = "HTTP 5XX ERRORS",
                    value = if (isDegraded) "184 Errors" else "12 Errors",
                    isElevated = isDegraded,
                    color = if (isDegraded) RiskCrimson else TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Metric 4: Throughput / CPU
                MetricCard(
                    title = "CPU UTILIZATION",
                    value = if (isDegraded) "88.4%" else "61.2%",
                    isElevated = isDegraded,
                    color = if (isDegraded) CanaryAmber else CyberCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    isElevated: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SurfaceDarkElevated, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isElevated) color.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
