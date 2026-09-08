package com.shipsafe.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.data.models.BlastRadiusDto
import com.shipsafe.ui.theme.*

@Composable
fun BlastRadiusGraph(
    blastRadius: BlastRadiusDto?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEPENDENCY TOPOLOGY",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${blastRadius?.totalBlastRadiusCount ?: 4} SERVICES AFFECTED",
                    color = RiskCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Node Layout Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(BackgroundDark, RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                // Connection Lines Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val rootPos = Offset(w * 0.5f, h * 0.42f)
                    val upstreamPos = Offset(w * 0.5f, h * 0.12f)
                    val leftChildPos = Offset(w * 0.22f, h * 0.82f)
                    val rightChildPos = Offset(w * 0.78f, h * 0.82f)

                    // Draw Edge Upstream -> Root
                    drawLine(
                        color = TextMuted.copy(alpha = 0.6f),
                        start = upstreamPos,
                        end = rootPos,
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Edge Root -> Left Child (Payment)
                    drawLine(
                        color = RiskCrimson,
                        start = rootPos,
                        end = leftChildPos,
                        strokeWidth = 4f
                    )

                    // Draw Edge Root -> Right Child (Orders)
                    drawLine(
                        color = CanaryAmber,
                        start = rootPos,
                        end = rightChildPos,
                        strokeWidth = 4f
                    )
                }

                // Upstream Node: API Gateway
                ServiceNodeBadge(
                    name = "API Gateway",
                    type = "CRITICAL UPSTREAM",
                    color = CyberCyan,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Root Target: Checkout Service
                ServiceNodeBadge(
                    name = blastRadius?.serviceName ?: "Checkout Service",
                    type = "TARGET SERVICE",
                    color = RiskCrimson,
                    isTarget = true,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Downstream 1: Payment Service
                ServiceNodeBadge(
                    name = "Payment Service",
                    type = "DIRECT DOWNSTREAM",
                    color = RiskCrimson,
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                // Downstream 2: Order Service
                ServiceNodeBadge(
                    name = "Order Service",
                    type = "CASCADING IMPACT",
                    color = CanaryAmber,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Blast Radius Summary Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BlastRadiusStatItem(label = "DIRECT IMPACT", value = "1 Service", color = RiskCrimson)
                BlastRadiusStatItem(label = "DOWNSTREAM", value = "${(blastRadius?.downstreamServicesCount ?: 3)} Services", color = CanaryAmber)
                BlastRadiusStatItem(label = "TOTAL RADIUS", value = "${(blastRadius?.totalBlastRadiusCount ?: 4)} Services", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun ServiceNodeBadge(
    name: String,
    type: String,
    color: Color,
    modifier: Modifier = Modifier,
    isTarget: Boolean = false
) {
    Column(
        modifier = modifier
            .background(
                if (isTarget) color.copy(alpha = 0.2f) else SurfaceDarkElevated,
                RoundedCornerShape(8.dp)
            )
            .border(
                if (isTarget) 2.dp else 1.dp,
                if (isTarget) color else color.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = type,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BlastRadiusStatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
