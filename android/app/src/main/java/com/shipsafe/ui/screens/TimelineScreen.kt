package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.theme.*

@Composable
fun TimelineScreen(
    deploymentId: String,
    onNavigateBack: () -> Unit
) {
    val timelineEvents = listOf(
        "10:32" to "Developer pushes commit 8f4c2e19d to main branch",
        "10:32" to "GitHub Actions pipeline triggers ShipSafe webhook",
        "10:33" to "Diff analysis detects 6 files changed (48 lines in payment.config.ts)",
        "10:33" to "Incident Memory matches SEV-1 incident INC-017 (92% similarity)",
        "10:33" to "AI risk synthesis calculates HIGH RISK (84/100) and recommends Canary 10%",
        "10:34" to "Push notification delivered to Release Manager iQOO device",
        "10:35" to "Release Manager reviews Blast Radius and selects Canary 10%",
        "10:36" to "Canary traffic live on pod replicas; health monitor polling active"
    )

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
                    text = "Deployment Lifecycle Timeline",
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
            items(timelineEvents.size) { index ->
                val (time, event) = timelineEvents[index]
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(CyberCyan, CircleShape)
                        )
                        if (index < timelineEvents.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(SurfaceBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(text = time, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = event, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
