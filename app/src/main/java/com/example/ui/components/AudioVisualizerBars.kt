package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent

@Composable
fun WaveformBars(
    isActive: Boolean,
    barColor: Color = CyanAccent,
    barCount: Int = 5,
    maxHeight: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        val transition = rememberInfiniteTransition(label = "wave")
        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 120)
            val heightFraction by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isActive) 1f else 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight$i"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(maxHeight * (if (isActive) heightFraction else 0.25f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
