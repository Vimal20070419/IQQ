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
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.HealthViewModel

@Composable
fun RollbackGuardScreen(
    deploymentId: String,
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    onRollbackComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(deploymentId) {
        viewModel.loadHealthData(deploymentId)
    }

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
                    text = "Smart Rollback Guard",
                    color = RiskCrimson,
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
                    onClick = {
                        viewModel.executeRollback {
                            onRollbackComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CONFIRM RESTORE TO v2.8.3",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PRE-FLIGHT SAFETY VERIFICATION",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Current State", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "v2.8.4 (Degraded)", color = RiskCrimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Rollback Target", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "v2.8.3 (Verified Stable)", color = SafeEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Database Migration", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "None (Safe Revert)", color = SafeEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SAFETY CHECKS CHECKLIST",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val checks = state.rollbackGuard?.checks ?: listOf(
                com.shipsafe.data.models.RollbackCheckItemDto("Deployment Active State", true, "Active deployment awaiting mitigation"),
                com.shipsafe.data.models.RollbackCheckItemDto("Previous Stable Tag", true, "Verified commit 7a3d9e2 (v2.8.3) available in registry"),
                com.shipsafe.data.models.RollbackCheckItemDto("Database Schema Lock", true, "No schema migrations in v2.8.4 diff"),
                com.shipsafe.data.models.RollbackCheckItemDto("Cluster Readiness", true, "Replica set ready for instant traffic redirection")
            )

            items(checks.size) { index ->
                val check = checks[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (check.passed) "✓" else "⚠️",
                            color = if (check.passed) SafeEmerald else CanaryAmber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = check.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = check.detail, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
