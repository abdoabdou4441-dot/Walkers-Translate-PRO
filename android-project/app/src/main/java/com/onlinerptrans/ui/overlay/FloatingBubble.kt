package com.onlinerptrans.ui.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.onlinerptrans.R
import com.onlinerptrans.ui.theme.GamingDark
import com.onlinerptrans.ui.theme.NeonCyan
import com.onlinerptrans.ui.theme.NeonCyanGlow

@Composable
fun FloatingBubbleContent(onBubbleClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(GamingDark, NeonCyanGlow)
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = NeonCyan.copy(alpha = borderAlpha),
                shape = CircleShape
            )
            .clickable { onBubbleClick() }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_bubble),
            contentDescription = "Translate",
            tint = NeonCyan,
            modifier = Modifier.size(36.dp)
        )
    }
}
