package com.onlinerptrans.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlinerptrans.MainViewModel
import com.onlinerptrans.ui.theme.*

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ── Logo ──────────────────────────────────────────────────────────────
        AppLogo()

        // ── App name + Developer branding ─────────────────────────────────────
        AppBranding()

        // ── Overlay permission card ───────────────────────────────────────────
        if (!uiState.hasOverlayPermission) {
            PermissionCard(
                icon        = Icons.Default.Layers,
                title       = "إذن العرض فوق التطبيقات",
                description = "يحتاج التطبيق إذن 'العرض فوق التطبيقات الأخرى' لعرض الفقاعة العائمة أثناء اللعب.",
                buttonText  = "منح الإذن",
                onGrantClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )
        }

        // ── Service control ───────────────────────────────────────────────────
        ServiceControlCard(
            isRunning            = uiState.isServiceRunning,
            hasOverlayPermission = uiState.hasOverlayPermission,
            onStart              = { viewModel.requestStartService(context) },
            onStop               = { viewModel.stopService(context) }
        )

        // ── How to use ────────────────────────────────────────────────────────
        HowToUseCard()

        // ── Features ─────────────────────────────────────────────────────────
        FeaturesCard()

        // ── Footer branding ───────────────────────────────────────────────────
        FooterBranding()

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ── Logo ──────────────────────────────────────────────────────────────────────

@Composable
private fun AppLogo() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(NeonCyanSubtle, GamingDark2)))
            .border(2.dp, NeonCyan.copy(0.6f), CircleShape)
    ) {
        Icon(
            imageVector        = Icons.Default.Translate,
            contentDescription = null,
            tint               = NeonCyan,
            modifier           = Modifier.size(44.dp)
        )
    }
}

// ── App name + hard-coded developer credit ────────────────────────────────────

@Composable
private fun AppBranding() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text          = "Online RP Trans V2 - REAL",
            color         = NeonCyan,
            fontSize      = 26.sp,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Text(
            text      = "مترجم شاشة فوري لألعاب RP",
            color     = TextSecondary,
            fontSize  = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text      = "Developed by Leo Walker",
            color     = NeonCyanDim.copy(alpha = 0.75f),
            fontSize  = 12.sp,
            fontStyle = FontStyle.Italic,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Generic permission card ───────────────────────────────────────────────────

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1A0A00))
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Text(title, color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
            Button(
                onClick  = onGrantClick,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    contentColor   = GamingBlack
                )
            ) {
                Icon(Icons.Default.SecurityUpdate, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Service control card ──────────────────────────────────────────────────────

@Composable
private fun ServiceControlCard(
    isRunning: Boolean,
    hasOverlayPermission: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = GamingDark2)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status dot + label
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) SuccessGreen else TextDisabled)
                )
                AnimatedContent(
                    targetState    = isRunning,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label          = "status_text"
                ) { running ->
                    Text(
                        text       = if (running) "الخدمة تعمل — OCR نشط" else "الخدمة متوقفة",
                        color      = if (running) SuccessGreen else TextSecondary,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Screen capture note
            if (!isRunning) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyanSubtle)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint     = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text      = "سيطلب التطبيق إذن التقاط الشاشة عند التشغيل",
                        color     = NeonCyanDim,
                        fontSize  = 12.sp,
                        lineHeight = 18.sp,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            // Start / Stop button
            Button(
                onClick  = if (isRunning) onStop else onStart,
                enabled  = hasOverlayPermission || isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = if (isRunning) ErrorRed else NeonCyan,
                    contentColor           = GamingBlack,
                    disabledContainerColor = GamingDark4,
                    disabledContentColor   = TextDisabled
                )
            ) {
                Icon(
                    imageVector        = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = if (isRunning) "إيقاف الخدمة" else "تشغيل الخدمة",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
        }
    }
}

// ── How to use ────────────────────────────────────────────────────────────────

@Composable
private fun HowToUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = GamingDark2)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "كيفية الاستخدام",
                color      = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp
            )
            HowToStep(1, Icons.Default.Layers,       "امنح إذن 'العرض فوق التطبيقات' من الإعدادات")
            HowToStep(2, Icons.Default.PlayArrow,    "اضغط 'تشغيل الخدمة' ثم وافق على إذن التقاط الشاشة")
            HowToStep(3, Icons.Default.SportsEsports,"افتح لعبة SAMP / Online RP Mobile")
            HowToStep(4, Icons.Default.TouchApp,     "اضغط على الفقاعة العائمة مرة واحدة لبدء الترجمة التلقائية")
            HowToStep(5, Icons.Default.Translate,    "الترجمة تظهر تلقائياً بدون أي تدخل منك")
            HowToStep(6, Icons.Default.TouchApp,     "اضغط الفقاعة مجدداً لإيقاف الالتقاط وإخفاء اللوحة")
        }
    }
}

@Composable
private fun HowToStep(num: Int, icon: ImageVector, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NeonCyanSubtle)
                .border(1.dp, NeonCyan.copy(0.4f), CircleShape)
        ) {
            Text(
                num.toString(),
                color      = NeonCyan,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(icon, null, tint = NeonCyanDim, modifier = Modifier.size(16.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

// ── Features ──────────────────────────────────────────────────────────────────

@Composable
private fun FeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = GamingDark2)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "المميزات",
                color      = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp
            )
            FeatureRow(Icons.Default.Screenshot,    "ترجمة تلقائية بالتقاط الشاشة (OCR) — بدون نسخ يدوي")
            FeatureRow(Icons.Default.Translate,     "ترجمة روسي → عربي بدون إنترنت (ML Kit)")
            FeatureRow(Icons.Default.MenuBook,      "قاموس مصطلحات RP (الجواز، الرخصة، البوليس...)")
            FeatureRow(Icons.Default.DragIndicator, "فقاعة عائمة قابلة للسحب — لا تتدخل مع التحكم باللعبة")
            FeatureRow(Icons.Default.ZoomOutMap,    "لوحة قابلة للتكبير والتصغير أثناء اللعب")
            FeatureRow(Icons.Default.Shield,        "كود محمي ومشفر بـ ProGuard / R8")
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

// ── Footer — hard-coded branding, non-removable ───────────────────────────────

@Composable
private fun FooterBranding() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier            = Modifier.padding(vertical = 8.dp)
    ) {
        HorizontalDivider(
            modifier  = Modifier.fillMaxWidth(0.5f),
            thickness = 1.dp,
            color     = NeonCyan.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text          = "Online RP Trans",
            color         = NeonCyan.copy(alpha = 0.6f),
            fontSize      = 12.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Text(
            text      = "Developed by Leo Walker",
            color     = TextDisabled,
            fontSize  = 11.sp,
            fontStyle = FontStyle.Italic
        )
    }
}
