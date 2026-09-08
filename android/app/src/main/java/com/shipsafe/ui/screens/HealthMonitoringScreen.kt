package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.shipsafe.ui.components.HealthLiveChart
import com.shipsafe.ui.components.SmartRollbackDialog
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.HealthViewModel

@Composable
fun HealthMonitoringScreen(
    deploymentId: String,
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRollbackGuard: (String) -> Unit,
    onNavigateToAudit: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showRollbackModal by remember { mutableStateOf(false) }

    LaunchedEffect(deploymentId) {
        viewModel.loadHealthData(deploymentId)
    }

    val isDegraded = state.healthData?.isDegraded == true

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Text(text = "←", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Post-Deploy Health Monitor",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToAudit) {
                    Text(text = "📋", fontSize = 16.sp)
                }
            }
        },
        bottomBar = {
            if (isDegraded) {
                Surface(
                    color = RedLightBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiskCrimson)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI SAFETY RECOMMENDATION: IMMEDIATE ROLLBACK",
                            color = RiskCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showRollbackModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "RESTORE STABLE VERSION (v2.8.3)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
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
            // Live Telemetry Chart Card
            item {
                HealthLiveChart(health = state.healthData)
            }

            // Degradation Alert Banner (If triggered)
            if (isDegraded) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RiskCrimson.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RiskCrimson)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🚨 CRITICAL SLA BREACH DETECTED",
                                color = RiskCrimson,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Error rate has spiked from 0.8% to 7.4% and P99 latency is 780ms. Stripe timeout cascading through order queues.",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Demo Controls: Trigger Degradation / Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "JURY DEMO TELEMETRY INJECTOR",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerDegradationSimulation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RiskCrimson),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "🚨 INJECT ERROR SPIKE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.resetHealthSimulation() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Text(text = "RESET NORMAL", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showRollbackModal) {
        SmartRollbackDialog(
            guard = state.rollbackGuard,
            onConfirm = {
                showRollbackModal = false
                viewModel.executeRollback {
                    onNavigateToAudit()
                }
            },
            onDismiss = { showRollbackModal = false }
        )
    }
}
