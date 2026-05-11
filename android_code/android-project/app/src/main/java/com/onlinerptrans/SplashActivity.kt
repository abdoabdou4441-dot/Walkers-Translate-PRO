package com.onlinerptrans

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.onlinerptrans.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            OnlineRPTransTheme {
                SplashScreenContent(
                    onFinished = {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreenContent(onFinished: () -> Unit) {
    val logoAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0.6f) }
    val textAlpha  = remember { Animatable(0f) }
    val creditAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo appears with bouncy spring
        launch { logoAlpha.animateTo(1f, tween(500)) }
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        delay(400)
        // Title fades in
        launch { textAlpha.animateTo(1f, tween(500)) }
        delay(300)
        // Developer credit fades in
        launch { creditAlpha.animateTo(1f, tween(500)) }

        // Hold for a moment then launch main app
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GamingDark2,
                        0.6f to GamingBlack,
                        1.0f to GamingBlack
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Animated Logo Icon ─────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(NeonCyanSubtle, GamingDark2))
                    )
                    .border(2.dp, NeonCyan.copy(0.7f), CircleShape)
            ) {
                Icon(
                    imageVector        = Icons.Default.Translate,
                    contentDescription = null,
                    tint               = NeonCyan,
                    modifier           = Modifier.size(60.dp)
                )
            }

            // ── App Name ───────────────────────────────────────────────────
            Column(
                horizontalAlignment  = Alignment.CenterHorizontally,
                verticalArrangement  = Arrangement.spacedBy(6.dp),
                modifier             = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text          = "Walkers Translate",
                    color         = NeonCyan,
                    fontSize      = 34.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    textAlign     = TextAlign.Center
                )
                Text(
                    text      = "مترجم شاشة فوري لألعاب RP",
                    color     = TextSecondary,
                    fontSize  = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            // ── Version badge ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                    .background(NeonCyanSubtle)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text      = "v2.0.0-PRO",
                    color     = NeonCyan,
                    fontSize  = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Developer credit — hard-coded at the bottom ────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .alpha(creditAlpha.value)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(1.dp)
                    .background(NeonCyan.copy(0.3f))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Developed by Leo Walker",
                color     = TextSecondary.copy(alpha = 0.8f),
                fontSize  = 13.sp,
                fontStyle = FontStyle.Italic
            )
            Text(
                text    = "Online RP Trans",
                color   = TextDisabled,
                fontSize = 11.sp
            )
        }
    }
}
