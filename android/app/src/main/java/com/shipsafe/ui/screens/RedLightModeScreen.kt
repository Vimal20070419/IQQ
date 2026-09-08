package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun RedLightModeScreen(
    deploymentId: String,
    viewModel: RiskDecisionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHealth: (String) -> Unit
) {
    Scaffold(
        containerColor = RedLightBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Text(text = "✕", color = RedLightAlert, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                Text(
                    text = "🚨 RED LIGHT EMERGENCY MODE",
                    color = RedLightAlert,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Emergency Header Alert
            Column {
                Text(
                    text = "PRODUCTION RELEASE BLOCKED",
                    color = RedLightAlert,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CHECKOUT SERVICE v2.8.4",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big High Risk Score Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RiskCrimson.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(2.dp, RiskCrimson, RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CRITICAL RISK",
                                color = RiskCrimson,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "84 / 100",
                                color = RiskCrimson,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "4 Downstream Services • INC-017 Similar Match",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // High-Impact Massive Touch Controls
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Button(
                    onClick = {
                        viewModel.executeDecision("CANARY", "Red Light emergency canary") {
                            onNavigateToHealth(deploymentId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CanaryAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "1. EXECUTE CANARY 10%",
                        color = BackgroundDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = {
                        viewModel.executeDecision("HOLD", "Red Light emergency hold") {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
                ) {
                    Text(
                        text = "2. HOLD DEPLOYMENT",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.executeDecision("ROLLBACK", "Red Light emergency immediate rollback") {
                            onNavigateToHealth(deploymentId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "3. EMERGENCY ROLLBACK",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
