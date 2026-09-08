package com.shipsafe.ui.screens

import androidx.compose.foundation.background
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
import com.shipsafe.ui.components.BlastRadiusGraph
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.RiskDecisionViewModel

@Composable
fun BlastRadiusScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(deploymentId) {
        if (state.blastRadius == null) {
            viewModel.loadDeploymentRisk(deploymentId)
        }
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
                    text = "Blast Radius Analysis",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
            // Visual Graph
            item {
                BlastRadiusGraph(blastRadius = state.blastRadius)
            }

            // Services Impact Details
            item {
                Text(
                    text = "AFFECTED SERVICE BREAKDOWN",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val nodes = state.blastRadius?.nodes ?: emptyList()
            items(nodes) { node ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = node.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = node.description,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        val color = when (node.type) {
                            "DIRECT" -> RiskCrimson
                            "CRITICAL_DEPENDENCY" -> CyberCyan
                            else -> CanaryAmber
                        }

                        Text(
                            text = node.type.replace("_", " "),
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
