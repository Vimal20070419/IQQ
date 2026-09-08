package com.shipsafe.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.components.*
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.RiskDecisionViewModel

@Composable
fun RiskDecisionHeroScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBlastRadius: (String) -> Unit,
    onNavigateToFailureChain: (String) -> Unit,
    onNavigateToStrategy: (String) -> Unit,
    onNavigateToHealth: (String) -> Unit,
    onNavigateToRollbackGuard: (String) -> Unit,
    onNavigateToTimeline: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var isWhyExpanded by remember { mutableStateOf(false) }
    var showRollbackConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(deploymentId) {
        viewModel.loadDeploymentRisk(deploymentId)
    }

    val riskScore = state.riskAssessment?.riskScore ?: 84
    val riskLevel = state.riskAssessment?.riskLevel ?: "HIGH"
    val recommendation = state.riskAssessment?.recommendation ?: "CANARY"

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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (state.deployment?.environment ?: "PRODUCTION").uppercase(),
                        color = RiskCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${state.deployment?.serviceName ?: "Checkout Service"} ${state.deployment?.version ?: "v2.8.4"}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { onNavigateToTimeline(deploymentId) }) {
                    Text(text = "⏱️", fontSize = 16.sp)
                }
            }
        },
        bottomBar = {
            // One-Handed Action Surface (Fixed at bottom)
            Surface(
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Canary Button (Primary AI Recommendation)
                        Button(
                            onClick = {
                                viewModel.executeDecision("CANARY", "AI recommended gradual 10% canary rollout") {
                                    onNavigateToHealth(deploymentId)
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CanaryAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "CANARY 10%",
                                color = BackgroundDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Approve Button
                        Button(
                            onClick = {
                                viewModel.executeDecision("APPROVE", "Approved 100% rollout") {
                                    onNavigateToHealth(deploymentId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "APPROVE",
                                color = BackgroundDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Hold Button
                        OutlinedButton(
                            onClick = {
                                viewModel.executeDecision("HOLD", "Deployment held for review") {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Text(text = "HOLD", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rollback Link (Requires Confirmation)
                    Text(
                        text = "Need emergency revert? Revert to previous version v2.8.3",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { onNavigateToRollbackGuard(deploymentId) }
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
            // 1. HERO RISK SCORE GAUGE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RiskPill(riskLevel = riskLevel)

                        Spacer(modifier = Modifier.height(14.dp))

                        RiskGauge(score = riskScore, size = 170.dp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "AI-ASSISTED RISK ASSESSMENT",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = state.riskAssessment?.summary
                                ?: "Payment configuration modified in production environment. A similar historical incident (INC-017) previously caused checkout failures.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 2. TRANSPARENT RISK CONTRIBUTING FACTORS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RISK SCORE BREAKDOWN (${riskScore}/100)",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val factors = state.riskAssessment?.factors ?: listOf(
                            com.shipsafe.data.models.RiskFactorDto("Payment configuration changed", 20, "SENSITIVE", "Modified payment.config.ts"),
                            com.shipsafe.data.models.RiskFactorDto("Similar historical incident (INC-017)", 18, "INCIDENT", "Matches INC-017 root cause"),
                            com.shipsafe.data.models.RiskFactorDto("Critical downstream dependencies", 16, "BLAST_RADIUS", "4 services affected"),
                            com.shipsafe.data.models.RiskFactorDto("Substantial code diff in core logic", 15, "DIFF", "184 lines changed"),
                            com.shipsafe.data.models.RiskFactorDto("Target is live Production environment", 15, "ENV", "100% traffic impact")
                        )

                        factors.forEach { factor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = factor.factor,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+${factor.points}",
                                    color = RiskCrimson,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. EXPANDABLE "WHY THIS RISK?" EVIDENCE PANEL
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isWhyExpanded = !isWhyExpanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍 WHY THIS RISK? (EVIDENCE BASED)",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isWhyExpanded) "▲ COLLAPSE" else "▼ EXPAND",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isWhyExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val evidenceList = state.riskAssessment?.evidence ?: listOf(
                                "Payment configuration modified in 2 sensitive files",
                                "184 lines changed across 6 files in critical transaction path",
                                "Similar historical incident INC-017 caused checkout outage",
                                "4 downstream services in direct blast radius",
                                "No database migration required (Safe Rollback available)"
                            )

                            evidenceList.forEach { ev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "✓ ", color = SafeEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = ev, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 4. POTENTIAL BLAST RADIUS PREVIEW
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBlastRadius(deploymentId) },
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
                                text = "POTENTIAL IMPACT RADIUS",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "FULL GRAPH →",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Checkout", "Payment", "Orders", "Inventory").forEach { svc ->
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceDarkElevated, RoundedCornerShape(6.dp))
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(text = svc, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // 5. WHAT COULD BREAK? (FAILURE CHAIN TEASER)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFailureChain(deploymentId) },
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
                                text = "WHAT COULD BREAK? (FAILURE PATH)",
                                color = RiskCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "VIEW CHAIN →",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Payment Config → Timeout → Retries → Queue Saturation → Customer Checkout Failure",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 6. WHAT-IF STRATEGY SIMULATOR SHORTCUT
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStrategy(deploymentId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WHAT-IF SIMULATOR",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Compare Normal (100%) vs Canary (10%) vs Hold (0%)",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Text(text = "SIMULATE →", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
