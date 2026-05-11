package com.onlinerptrans.ui.overlay

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun TranslationPanelContent(
    service: FloatingOverlayService,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OverlayBg)
                .border(
                    width = 1.dp,
                    color = NeonCyan.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Header ────────────────────────────────────────────────────
                PanelHeader(onClose = onClose)

                // ── Tab Row ───────────────────────────────────────────────────
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = GamingDark2,
                    contentColor     = NeonCyan,
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(2.dp)
                                .background(NeonCyan)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick  = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Translate, null, modifier = Modifier.size(16.dp))
                                Text("ترجمة", fontSize = 13.sp)
                            }
                        },
                        selectedContentColor   = NeonCyan,
                        unselectedContentColor = TextSecondary
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick  = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                                Text("القاموس", fontSize = 13.sp)
                            }
                        },
                        selectedContentColor   = NeonCyan,
                        unselectedContentColor = TextSecondary
                    )
                }

                // ── Content ───────────────────────────────────────────────────
                when (selectedTab) {
                    0 -> TranslateTab(service = service)
                    1 -> DictionaryTab()
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PanelHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = TextSecondary
            )
        }
        Text(
            text       = "Online RP Trans",
            style      = MaterialTheme.typography.titleMedium,
            color      = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp
        )
        Box(modifier = Modifier.size(32.dp)) // Spacer to balance close button
    }
}

// ── Translate Tab ─────────────────────────────────────────────────────────────

@Composable
private fun TranslateTab(service: FloatingOverlayService) {
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Input field
        InputField(
            value    = service.inputText,
            onValueChange = {
                service.inputText = it
                if (it.isBlank()) service.outputText = ""
            },
            onClear  = { service.inputText = ""; service.outputText = "" },
            onTranslate = { service.performTranslation(service.inputText) }
        )

        // Output field
        OutputField(
            text         = service.outputText,
            isTranslating = service.isTranslating,
            onCopy = {
                if (service.outputText.isNotBlank()) {
                    clipboard.setText(AnnotatedString(service.outputText))
                }
            }
        )
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onTranslate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("النص الروسي", color = TextSecondary, fontSize = 12.sp)
            if (value.isNotBlank()) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GamingDark3)
                .border(1.dp, GamingDark4, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    "الصق النص الروسي هنا أو انسخه تلقائياً...",
                    color = TextDisabled,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                BasicTextField(
                    value    = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color     = TextPrimary,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Start
                    ),
                    cursorBrush = SolidColor(NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        Button(
            onClick  = onTranslate,
            enabled  = value.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = NeonCyan,
                contentColor           = GamingBlack,
                disabledContainerColor = GamingDark4
            )
        ) {
            Icon(Icons.Default.Translate, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ترجم", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun OutputField(
    text: String,
    isTranslating: Boolean,
    onCopy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الترجمة إلى العربية", color = TextSecondary, fontSize = 12.sp)
            if (text.isNotBlank()) {
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "نسخ",
                        tint = NeonCyanDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color    = NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Text("جاري الترجمة...", color = NeonCyanDim, fontSize = 13.sp)
                    }
                }
                text.isNotBlank() -> {
                    Text(
                        text     = text,
                        color    = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        textAlign = TextAlign.Start
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DictionaryCategory?>(null) }

    val results by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            val base = if (selectedCategory != null)
                GamerDictionary.byCategory(selectedCategory!!)
            else
                GamerDictionary.entries
            if (searchQuery.isBlank()) base
            else base.filter { e ->
                e.russian.lowercase().contains(searchQuery.lowercase()) ||
                e.arabic.contains(searchQuery) ||
                e.darija.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GamingDark3)
                .border(1.dp, GamingDark4, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                if (searchQuery.isEmpty()) {
                    Text("ابحث في القاموس...", color = TextDisabled, fontSize = 13.sp)
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                )
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

        // Entries
        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(results, key = { "${it.russian}${it.category}" }) { entry ->
                DictionaryEntryRow(entry = entry)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label, fontSize = 11.sp) },
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor     = NeonCyanSubtle,
            selectedLabelColor         = NeonCyan,
            containerColor             = GamingDark3,
            labelColor                 = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled         = true,
            selected        = selected,
            selectedBorderColor = NeonCyan.copy(0.5f),
            borderColor     = GamingDark4
        )
    )
}

@Composable
private fun DictionaryEntryRow(entry: DictionaryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GamingDark3)
            .border(1.dp, GamingDark4, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Arabic / Darija (RTL side)
        Column(horizontalAlignment = Alignment.End) {
            Text(entry.arabic, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (entry.darija != entry.arabic) {
                Text(entry.darija, color = NeonCyanDim, fontSize = 11.sp)
            }
        }

        // Separator
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(30.dp)
                .background(GamingDark4)
        )

        // Russian (LTR side)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text  = entry.russian,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
