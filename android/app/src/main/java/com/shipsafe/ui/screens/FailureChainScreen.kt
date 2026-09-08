package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.components.FailureChainStepCard
import com.shipsafe.ui.theme.*
import com.shipsafe.viewmodel.RiskDecisionViewModel

@Composable
fun FailureChainScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(deploymentId) {
        if (state.failureChain == null) {
            viewModel.loadDeploymentRisk(deploymentId)
        }
    }

    val steps = state.failureChain?.steps ?: state.riskAssessment?.failureChain ?: emptyList()

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
                    text = "What Could Break?",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AI-GENERATED POTENTIAL FAILURE PATH",
                            color = RiskCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This simulates the cascading degradation path if payment latency exceeds timeout limits. Grounded in historical incident INC-017.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            itemsIndexed(steps) { index, step ->
                FailureChainStepCard(
                    step = step,
                    isLast = index == steps.size - 1
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
