package com.onlinerptrans.ui.overlay

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlinerptrans.data.DictionaryCategory
import com.onlinerptrans.data.DictionaryEntry
import com.onlinerptrans.data.GamerDictionary
import com.onlinerptrans.service.FloatingOverlayService
import com.onlinerptrans.ui.theme.*

private const val PREFS_NAME    = "user_dictionary"
private const val KEY_USER_DICT = "user_entries"

@Composable
fun TranslationPanelContent(service: FloatingOverlayService) {
    var selectedTab by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Scale Slider ─────────────────────────────────────────────────
            ScaleSlider(
                scale    = service.panelScale,
                onChange = { service.panelScale = it }
            )

            // ── Scalable Panel Body ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(service.panelScale)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(OverlayBg)
                    .border(
                        width = 1.dp,
                        color = NeonCyan.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // ── Header ────────────────────────────────────────────────
                    PanelHeader()

                    // ── Tab Row ───────────────────────────────────────────────
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor   = GamingDark2,
                        contentColor     = NeonCyan,
                        edgePadding      = 0.dp,
                        indicator = { tabPositions ->
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(2.dp)
                                    .background(NeonCyan)
                            )
                        },
                        divider = {}
                    ) {
                        val tabs = listOf(
                            "ترجمة" to Icons.Default.Translate,
                            "السجل" to Icons.Default.History,
                            "القاموس" to Icons.Default.MenuBook,
                            "الإعدادات" to Icons.Default.Settings
                        )
                        tabs.forEachIndexed { index, (title, icon) ->
                            Tab(
                                selected = selectedTab == index,
                                onClick  = { selectedTab = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(icon, null, modifier = Modifier.size(16.dp))
                                        Text(title, fontSize = 12.sp)
                                    }
                                },
                                selectedContentColor   = NeonCyan,
                                unselectedContentColor = TextSecondary
                            )
                        }
                    }

                    // ── Content ───────────────────────────────────────────────
                    when (selectedTab) {
                        0 -> TranslateTab(service = service)
                        1 -> HistoryTab(service = service)
                        2 -> DictionaryTab()
                        3 -> SettingsTab(service = service)
                    }

                    // ── Permanent Branding Footer (hard-coded, non-removable) ─
                    BrandingFooter()
                }
            }
        }
    }
}

// ── Scale Slider ─────────────────────────────────────────────────────────────

@Composable
private fun ScaleSlider(scale: Float, onChange: (Float) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GamingDark2.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("حجم", color = TextSecondary, fontSize = 11.sp)
            Slider(
                value         = scale,
                onValueChange = onChange,
                valueRange    = 0.6f..1.4f,
                modifier      = Modifier.weight(1f).height(24.dp),
                colors        = SliderDefaults.colors(
                    thumbColor            = NeonCyan,
                    activeTrackColor      = NeonCyan,
                    inactiveTrackColor    = GamingDark4
                )
            )
            Text(
                text     = "${(scale * 100).toInt()}%",
                color    = NeonCyanDim,
                fontSize = 11.sp,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun PanelHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text       = "Online RP Trans",
            style      = MaterialTheme.typography.titleMedium,
            color      = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp
        )
    }
}

// ── Translate Tab ─────────────────────────────────────────────────────────────

@Composable
private fun TranslateTab(service: FloatingOverlayService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // OCR detected text (read-only — screen capture only, no manual input)
        OcrDetectedBox(text = service.ocrText)

        // Translation output
        OutputField(
            text          = service.outputText,
            isTranslating = service.isTranslating
        )

        // Capture status indicator
        if (service.isCaptureActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00FF88), shape = RoundedCornerShape(50))
                )
                Spacer(Modifier.width(6.dp))
                Text("التقاط الشاشة نشط", color = Color(0xFF00FF88), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun OcrDetectedBox(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("النص المكتشف من الشاشة", color = TextSecondary, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp, max = 120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GamingDark3)
                .border(1.dp, GamingDark4, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    "في انتظار التقاط النص من الشاشة...",
                    color    = TextDisabled,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text      = text,
                        color     = TextSecondary,
                        fontSize  = 13.sp,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputField(text: String, isTranslating: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("الترجمة إلى العربية", color = TextSecondary, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GamingDark2)
                .border(
                    width = 1.dp,
                    color = if (text.isNotBlank()) NeonCyan.copy(0.3f) else GamingDark4,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isTranslating -> {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            color       = NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Text("جاري الترجمة...", color = NeonCyanDim, fontSize = 13.sp)
                    }
                }
                text.isNotBlank() -> {
                    Text(
                        text       = text,
                        color      = TextPrimary,
                        fontSize   = 15.sp,
                        lineHeight = 22.sp,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        textAlign  = TextAlign.Start
                    )
                }
                else -> {
                    Text("ستظهر الترجمة هنا", color = TextDisabled, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Dictionary Tab ────────────────────────────────────────────────────────────

@Composable
private fun DictionaryTab() {
    val context = LocalContext.current
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DictionaryCategory?>(null) }
    var showAddDialog    by remember { mutableStateOf(false) }

    val userEntries = remember { mutableStateListOf<DictionaryEntry>().also { list ->
        list.addAll(loadUserEntries(context))
    }}

    val results by remember(searchQuery, selectedCategory, userEntries.size) {
        derivedStateOf {
            val builtIn = if (selectedCategory != null)
                GamerDictionary.byCategory(selectedCategory!!)
            else
                GamerDictionary.entries

            val user = if (selectedCategory == null) userEntries.toList() else emptyList()
            val all  = builtIn + user

            if (searchQuery.isBlank()) all
            else all.filter { e ->
                e.russian.lowercase().contains(searchQuery.lowercase()) ||
                e.arabic.contains(searchQuery) ||
                e.darija.contains(searchQuery)
            }
        }
    }

    if (showAddDialog) {
        AddPhraseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { russian, arabic, darija ->
                val entry = DictionaryEntry(russian, arabic, darija, DictionaryCategory.COMMUNICATION)
                userEntries.add(entry)
                saveUserEntries(context, userEntries)
                showAddDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar + Add button
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GamingDark3)
                    .border(1.dp, GamingDark4, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    if (searchQuery.isEmpty()) {
                        Text("ابحث في القاموس...", color = TextDisabled, fontSize = 13.sp)
                    }
                    BasicTextField(
                        value         = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle     = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush   = SolidColor(NeonCyan),
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            }
            IconButton(
                onClick  = { showAddDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonCyanSubtle)
            ) {
                Icon(Icons.Default.Add, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
        }

        // Category chips
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                CategoryChip(
                    label    = "الكل",
                    selected = selectedCategory == null,
                    onClick  = { selectedCategory = null }
                )
            }
            items(DictionaryCategory.values()) { cat ->
                CategoryChip(
                    label    = cat.labelAr,
                    selected = selectedCategory == cat,
                    onClick  = { selectedCategory = if (selectedCategory == cat) null else cat }
                )
            }
        }

        // Entries list
        LazyColumn(
            modifier            = Modifier.heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(results, key = { "${it.russian}${it.category}${it.arabic}" }) { entry ->
                val isUserEntry = userEntries.contains(entry)
                DictionaryEntryRow(
                    entry       = entry,
                    isUserEntry = isUserEntry,
                    onDelete    = if (isUserEntry) {
                        {
                            userEntries.remove(entry)
                            saveUserEntries(context, userEntries)
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun AddPhraseDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var russian by remember { mutableStateOf("") }
    var arabic  by remember { mutableStateOf("") }
    var darija  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = GamingDark2,
        title = {
            Text("إضافة عبارة شخصية", color = NeonCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogInput(value = russian, placeholder = "النص بالروسية", onValueChange = { russian = it })
                DialogInput(value = arabic,  placeholder = "الترجمة بالعربية", onValueChange = { arabic  = it })
                DialogInput(value = darija,  placeholder = "الدارجة (اختياري)", onValueChange = { darija  = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (russian.isNotBlank() && arabic.isNotBlank()) onConfirm(russian, arabic, darija.ifBlank { arabic }) },
                enabled  = russian.isNotBlank() && arabic.isNotBlank()
            ) {
                Text("إضافة", color = NeonCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DialogInput(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GamingDark3)
            .border(1.dp, GamingDark4, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = TextDisabled, fontSize = 13.sp)
        }
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            textStyle     = TextStyle(color = TextPrimary, fontSize = 13.sp),
            cursorBrush   = SolidColor(NeonCyan),
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label, fontSize = 11.sp) },
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonCyanSubtle,
            selectedLabelColor     = NeonCyan,
            containerColor         = GamingDark3,
            labelColor             = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled             = true,
            selected            = selected,
            selectedBorderColor = NeonCyan.copy(0.5f),
            borderColor         = GamingDark4
        )
    )
}

@Composable
private fun DictionaryEntryRow(
    entry: DictionaryEntry,
    isUserEntry: Boolean,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUserEntry) GamingDark3.copy(alpha = 0.7f) else GamingDark3)
            .border(
                width = 1.dp,
                color = if (isUserEntry) NeonCyan.copy(0.15f) else GamingDark4,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(entry.arabic, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (entry.darija != entry.arabic) {
                Text(entry.darija, color = NeonCyanDim, fontSize = 11.sp)
            }
        }

        Box(modifier = Modifier.width(1.dp).height(30.dp).background(GamingDark4))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(entry.russian, color = TextSecondary, fontSize = 13.sp)
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, null, tint = TextDisabled, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── Permanent Branding Footer — hard-coded, non-removable ─────────────────────

@Composable
private fun HistoryTab(service: FloatingOverlayService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("آخر 20 ترجمة", color = TextSecondary, fontSize = 12.sp)
        
        if (service.translationHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا يوجد سجل ترجمة بعد", color = TextDisabled, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(service.translationHistory.size) { index ->
                    val entry = service.translationHistory[index]
                    HistoryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: FloatingOverlayService.HistoryEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GamingDark3)
            .border(1.dp, GamingDark4, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(entry.original, color = TextSecondary, fontSize = 12.sp, fontStyle = FontStyle.Italic)
        }
        Divider(color = GamingDark4, thickness = 0.5.dp)
        Text(entry.translated, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsTab(service: FloatingOverlayService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auto-Translate Toggle
        SettingsToggle(
            title = "الترجمة التلقائية (PRO)",
            subtitle = "ترجمة تلقائية كل 3-5 ثوانٍ",
            checked = service.isAutoTranslateEnabled,
            onCheckedChange = { service.toggleAutoTranslate(it) }
        )

        Divider(color = GamingDark4)

        // Bubble Customization
        Text("تخصيص الفقاعة العائمة", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("حجم الفقاعة: ${service.bubbleSize.toInt()}dp", color = TextSecondary, fontSize = 12.sp)
            Slider(
                value = service.bubbleSize,
                onValueChange = { service.updateBubbleSize(it) },
                valueRange = 40f..100f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("شفافية الفقاعة: ${(service.bubbleAlpha * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
            Slider(
                value = service.bubbleAlpha,
                onValueChange = { service.updateBubbleAlpha(it) },
                valueRange = 0.3f..1f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyanSubtle,
                uncheckedThumbColor = TextDisabled,
                uncheckedTrackColor = GamingDark4
            )
        )
    }
}

@Composable
private fun BrandingFooter() {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .background(GamingDark2)
            .padding(vertical = 8.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text       = "Online RP Trans",
            color      = NeonCyan.copy(alpha = 0.9f),
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Text(
            text      = "Developed by Leo Walker",
            color     = TextSecondary.copy(alpha = 0.7f),
            fontSize  = 10.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

// ── SharedPreferences helpers for user dictionary ─────────────────────────────

private fun loadUserEntries(context: Context): List<DictionaryEntry> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw   = prefs.getString(KEY_USER_DICT, "") ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.split("||").mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size == 3)
            DictionaryEntry(parts[0], parts[1], parts[2], DictionaryCategory.COMMUNICATION)
        else null
    }
}

private fun saveUserEntries(context: Context, entries: List<DictionaryEntry>) {
    val raw = entries.joinToString("||") { "${it.russian}|${it.arabic}|${it.darija}" }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_USER_DICT, raw).apply()
}
