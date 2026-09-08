package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.components.DeploymentCard
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.AuthViewModel
import com.shipsafe.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    onNavigateToRiskDecision: (String) -> Unit,
    onNavigateToActiveDeployments: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToRedLight: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by mainViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.loadDashboardData()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ShipSafe",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Deployment Safety Center",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emergency Red Light Button
                    IconButton(
                        onClick = { onNavigateToRedLight("dep-checkout-v284") },
                        modifier = Modifier
                            .background(RiskCrimson.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, RiskCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .size(36.dp)
                    ) {
                        Text(text = "🚨", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .size(36.dp)
                    ) {
                        Text(text = "⚙️", fontSize = 16.sp)
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
            // Hero Live Demo Banner for Judges
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRiskDecision("dep-checkout-v284") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ HACKATHON HERO DEMO",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Checkout Service v2.8.4 (High Risk 84/100)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Canary → Degradation → Smart Rollback Flow",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { onNavigateToRiskDecision("dep-checkout-v284") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "START", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "ACTIVE",
                        count = "${state.activeCount}",
                        color = TextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToActiveDeployments() }
                    )
                    StatCard(
                        title = "HIGH RISK",
                        count = "${state.highRiskCount}",
                        color = RiskCrimson,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToActiveDeployments() }
                    )
                    StatCard(
                        title = "DECISION PENDING",
                        count = "${state.pendingDecisionCount}",
                        color = CanaryAmber,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToActiveDeployments() }
                    )
                }
            }

            // Section: Active Deployments Awaiting Decision
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEPLOYMENTS AWAITING DECISION",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "VIEW ALL",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToActiveDeployments() }
                    )
                }
            }

            // Deployments List
            items(state.deployments) { deployment ->
                DeploymentCard(
                    deployment = deployment,
                    onClick = { onNavigateToRiskDecision(deployment.id) }
                )
            }

            // Quick Navigation Shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToIncidents,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text(text = "🧠 Incidents (18)", color = TextSecondary, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToAudit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text(text = "📋 Audit Trail", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = count, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}
