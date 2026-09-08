package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.RiskDecisionViewModel

@Composable
fun StrategySimulatorScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit,
    onExecuteStrategy: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedStrategy by remember { mutableStateOf("CANARY") }
    var trafficSlider by remember { mutableFloatStateOf(10f) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Text(text = "←", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "What If I Ship This?",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            Surface(
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Button(
                    onClick = { onExecuteStrategy(selectedStrategy) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStrategy == "CANARY") CanaryAmber else SafeEmerald
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "APPLY STRATEGY: $selectedStrategy (${if (selectedStrategy == "CANARY") "${trafficSlider.toInt()}%" else if (selectedStrategy == "NORMAL") "100%" else "0%"})",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SIMULATE RELEASE BLAST EXPOSURE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Option 1: CANARY (RECOMMENDED)
            item {
                StrategyOptionCard(
                    title = "CANARY ROLLOUT",
                    subtitle = "Recommended by AI Safety Engine",
                    traffic = "${trafficSlider.toInt()}% of live user traffic",
                    exposureRisk = "LOWER (Max 10% users exposed during initial 5m soak)",
                    isRecommended = true,
                    isSelected = selectedStrategy == "CANARY",
                    color = CanaryAmber,
                    onClick = { selectedStrategy = "CANARY" }
                )
            }

            // Option 2: NORMAL DEPLOYMENT
            item {
                StrategyOptionCard(
                    title = "NORMAL (DIRECT 100%)",
                    subtitle = "Standard immediate container replacement",
                    traffic = "100% of all user traffic",
                    exposureRisk = "HIGH (Immediate impact on all checkout transactions)",
                    isRecommended = false,
                    isSelected = selectedStrategy == "NORMAL",
                    color = RiskCrimson,
                    onClick = { selectedStrategy = "NORMAL" }
                )
            }

            // Option 3: HOLD
            item {
                StrategyOptionCard(
                    title = "HOLD RELEASE",
                    subtitle = "Pause rollout until on-call investigation finishes",
                    traffic = "0% (Keep existing v2.8.3 container active)",
                    exposureRisk = "ZERO (Zero user risk, delay release)",
                    isRecommended = false,
                    isSelected = selectedStrategy == "HOLD",
                    color = CyberCyan,
                    onClick = { selectedStrategy = "HOLD" }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StrategyOptionCard(
    title: String,
    subtitle: String,
    traffic: String,
    exposureRisk: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceDarkElevated else SurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) color else SurfaceBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (isRecommended) {
                    Text(text = "⭐ RECOMMENDED", color = CanaryAmber, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Traffic: $traffic", color = TextSecondary, fontSize = 12.sp)
            Text(text = "Risk Impact: $exposureRisk", color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
