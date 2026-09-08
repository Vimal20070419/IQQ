package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.shipsafe.ui.components.RiskPill
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.RiskDecisionViewModel

@Composable
fun DeploymentDetailScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRiskDecision: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(deploymentId) {
        viewModel.loadDeploymentRisk(deploymentId)
    }

    val dep = state.deployment

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
                    text = "Release Diff & Metadata",
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
                    onClick = { onNavigateToRiskDecision(deploymentId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "EVALUATE RISK DECISION →", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = dep?.serviceName ?: "Checkout Service", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            RiskPill(riskLevel = state.riskAssessment?.riskLevel ?: "HIGH")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Commit: ${dep?.commitSha ?: "8f4c2e19d"}", color = CyberCyan, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Text(text = dep?.commitMessage ?: "feat(checkout): optimize payment gateway retry logic", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Author: ${dep?.author ?: "jordan.dev@shipsafe.io"}", color = TextMuted, fontSize = 11.sp)
                        Text(text = "Branch: ${dep?.branch ?: "main"} • Repo: ${dep?.repository ?: "shipsafe-org/checkout-service"}", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            item {
                Text(
                    text = "CHANGED FILES (${dep?.files?.size ?: 6})",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val files = dep?.files ?: emptyList()
            items(files) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.filePath, color = TextPrimary, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Text(text = "${file.category} • ${file.linesChanged} lines", color = TextMuted, fontSize = 10.sp)
                        }

                        if (file.isSensitive) {
                            Text(text = "SENSITIVE", color = RiskCrimson, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
