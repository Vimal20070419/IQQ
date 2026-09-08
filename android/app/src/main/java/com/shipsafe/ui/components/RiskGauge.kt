package com.shipsafe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shipsafe.ui.theme.*

@Composable
fun RiskGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    var animatedScore by remember { mutableStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedScore,
        animationSpec = tween(durationMillis = 1200),
        label = "riskScoreProgress"
    )

    LaunchedEffect(score) {
        animatedScore = score / 100f
    }

    val riskColor = when {
        score >= 75 -> RiskCrimson
        score >= 45 -> CanaryAmber
        else -> SafeEmerald
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Track Arc (240 degrees sweep)
            drawArc(
                color = SurfaceBorder,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(SafeEmerald, CanaryAmber, RiskCrimson, RiskCrimson)
                ),
                startAngle = 150f,
                sweepAngle = 240f * progressAnimation,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(progressAnimation * 100).toInt()}",
                color = riskColor,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                text = "OUT OF 100",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
