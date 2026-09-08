package com.shipsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.shipsafe.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var selectedRole by remember { mutableStateOf("RELEASE_MANAGER") }
    val roles = listOf("RELEASE_MANAGER", "ON_CALL_ENGINEER", "ADMIN", "VIEWER")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "ShipSafe",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Enterprise SRE & Release Decision Portal",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fast Demo Role Selector
        Text(
            text = "SELECT OPERATIONAL ROLE",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        roles.forEach { role ->
            val isSelected = selectedRole == role
            val (roleTitle, roleDesc) = when (role) {
                "RELEASE_MANAGER" -> "Release Manager (Full Release & Canary Rights)" to "Sarah Chen"
                "ON_CALL_ENGINEER" -> "On-Call SRE (Rollback & Incident Mitigation)" to "David Miller"
                "ADMIN" -> "Platform Administrator (Global Access)" to "Alex Mercer"
                else -> "DevOps Observer (Read Only)" to "Elena Rostova"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(
                        if (isSelected) CyberCyan.copy(alpha = 0.12f) else SurfaceDark,
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) CyberCyan else SurfaceBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { selectedRole = role }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = roleTitle,
                            color = if (isSelected) CyberCyan else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = roleDesc,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (isSelected) {
                        Text(text = "✓", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Main 1-Click Login Button
        Button(
            onClick = {
                authViewModel.loginWithDemo(selectedRole)
                onLoginSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "ACCESS DEPLOYMENT SAFETY CENTER",
                color = BackgroundDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Optimized for iQOO Phone • Offline Safe",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
