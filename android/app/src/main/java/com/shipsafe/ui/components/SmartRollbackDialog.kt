package com.shipsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shipsafe.data.models.RollbackGuardDto
import com.shipsafe.ui.theme.*

@Composable
fun SmartRollbackDialog(
    guard: RollbackGuardDto?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, RiskCrimson)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🚨 SMART ROLLBACK GUARD",
                    color = RiskCrimson,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Verify pre-flight conditions before reverting live traffic.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Version Verification
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark, RoundedCornerShape(8.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "CURRENT VERSION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "TARGET RESTORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = guard?.currentVersion ?: "v2.8.4", color = RiskCrimson, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "→  ${guard?.targetVersion ?: "v2.8.3"}", color = SafeEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pre-flight Checks Checklist
                guard?.checks?.forEach { check ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (check.passed) "✓ " else "⚠️ ",
                            color = if (check.passed) SafeEmerald else CanaryAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = check.detail,
                            color = if (check.passed) TextPrimary else CanaryAmber,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text(text = "CANCEL", color = TextSecondary)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "CONFIRM ROLLBACK", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
