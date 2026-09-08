package com.shipsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.theme.*

@Composable
fun EvidenceBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CyberCyan,
    isGrounded: Boolean = true
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGrounded) {
            Text(
                text = "✓ ",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RiskPill(
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (riskLevel.uppercase()) {
        "HIGH", "CRITICAL" -> Triple(RiskCrimson.copy(alpha = 0.15f), RiskCrimson, "HIGH RISK")
        "MEDIUM" -> Triple(CanaryAmber.copy(alpha = 0.15f), CanaryAmber, "MEDIUM RISK")
        else -> Triple(SafeEmerald.copy(alpha = 0.15f), SafeEmerald, "LOW RISK")
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
