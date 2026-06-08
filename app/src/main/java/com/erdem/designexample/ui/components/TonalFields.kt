package com.erdem.designexample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdem.designexample.ui.theme.AppPalette
import java.time.LocalDate
import java.time.YearMonth

// ─── Paylaşılan sabitler ───────────────────────────────────────────────────

private val FieldShape = RoundedCornerShape(14.dp)

private val trMonths = arrayOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)

/** Tarihi "12 Mayıs 2024" biçiminde gösterir. */
fun formatTrDate(date: LocalDate): String =
    "${date.dayOfMonth} ${trMonths[date.monthValue - 1]} ${date.year}"

/** 40+ kitle için biraz büyütülmüş, okunaklı alan metni. */
@Composable
private fun fieldTextStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)

/** Koyu navy palete uygun, dolgulu (tonal) alan renk seti. */
@Composable
private fun tonalFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor      = AppPalette.Field,
    unfocusedContainerColor    = AppPalette.Field,
    disabledContainerColor     = AppPalette.Field,
    errorContainerColor        = AppPalette.Field,
    focusedIndicatorColor      = Color.Transparent,
    unfocusedIndicatorColor    = Color.Transparent,
    disabledIndicatorColor     = Color.Transparent,
    errorIndicatorColor        = Color.Transparent,
    focusedTextColor           = AppPalette.TextPrimary,
    unfocusedTextColor         = AppPalette.TextPrimary,
    disabledTextColor          = AppPalette.TextPrimary,
    focusedLabelColor          = AppPalette.Teal,
    unfocusedLabelColor        = AppPalette.TextSecondary,
    disabledLabelColor         = AppPalette.TextSecondary,
    focusedLeadingIconColor    = AppPalette.Teal,
    unfocusedLeadingIconColor  = AppPalette.TextSecondary,
    disabledLeadingIconColor   = AppPalette.TextSecondary,
    focusedTrailingIconColor   = AppPalette.TextSecondary,
    unfocusedTrailingIconColor = AppPalette.TextSecondary,
    cursorColor                = AppPalette.Teal
)

// ─── TonalTextField ───────────────────────────────────────────────────────

/** Dolgulu metin alanı (form ekranları için). */
@Composable
fun TonalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        shape = FieldShape,
        textStyle = fieldTextStyle(),
        colors = tonalFieldColors(),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = modifier
    )
}

// ─── TonalAutocompleteField ───────────────────────────────────────────────

/**
 * Otomatik tamamlamalı dolgulu metin alanı. Yazıldıkça [suggestions] içinden eşleşenler
 * (büyük/küçük harf duyarsız, "içeren") öneri listesi olarak açılır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonalAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    var expanded by remember { mutableStateOf(false) }
    val query    = value.trim()
    val filtered = suggestions.filter {
        it.contains(query, ignoreCase = true) && !it.equals(query, ignoreCase = true)
    }
    val open = expanded && filtered.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = open,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            shape = FieldShape,
            textStyle = fieldTextStyle(),
            colors = tonalFieldColors(),
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = open,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppPalette.Card)
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion, color = AppPalette.TextPrimary) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = AppPalette.TextPrimary)
                )
            }
        }
    }
}

// ─── TonalDropdownField ───────────────────────────────────────────────────

/** Dolgulu açılır menü alanı (örn. Sürüm). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonalDropdownField(
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            shape = FieldShape,
            textStyle = fieldTextStyle(),
            colors = tonalFieldColors(),
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppPalette.Card)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (option == value) AppPalette.Teal else AppPalette.TextPrimary,
                            fontWeight = if (option == value) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = AppPalette.TextPrimary)
                )
            }
        }
    }
}

// ─── Tarih seçici ─────────────────────────────────────────────────────────

private val WheelItemHeight = 52.dp
private const val WheelVisible = 5  // kaç öğe gösterilir (tek sayı — ortası seçili)

/**
 * Kaydırılabilir tekerlek (drum-roll) seçici.
 * [items] listesinde parmakla yukarı/aşağı kaydırarak seçim yapılır.
 * Ortadaki öğe seçili olarak vurgulanır; fling ile snap davranışı vardır.
 *
 * @param items         Seçenek metinleri.
 * @param selectedIndex Şu an seçili öğenin 0-tabanlı indeksi.
 * @param onSelect      Yeni seçim yapılınca yeni indeks bildirilir.
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val half      = WheelVisible / 2
    val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = safeIndex
    )
    val fling = rememberSnapFlingBehavior(listState)

    // rememberUpdatedState ile closure'ların her zaman güncel değerleri görmesini sağla
    val currentOnSelect by androidx.compose.runtime.rememberUpdatedState(onSelect)
    val currentSelectedIndex by androidx.compose.runtime.rememberUpdatedState(selectedIndex)

    // Harici selectedIndex değişince (ay değişip gün sınırı küçülünce vb.) listeyi kaydır.
    // Kullanıcının kendi kaydırmasından gelen onSelect sonrası gereksiz yeniden kaydırmayı
    // önlemek için, önce mevcut merkez öğeyi kontrol ediyoruz.
    LaunchedEffect(safeIndex) {
        val info = listState.layoutInfo
        // item.offset, contentPadding sonrasına göre hesaplanır;
        // viewportCenter'ı da aynı referans çerçevesine taşıyoruz.
        val viewportCenter = info.viewportSize.height / 2 - info.beforeContentPadding
        val currentCenter = info.visibleItemsInfo.minByOrNull { item ->
            kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
        }?.index
        // Zaten doğru öğeye odaklanmışsa kaydırma yapma
        if (currentCenter != safeIndex) {
            listState.animateScrollToItem(safeIndex)
        }
    }

    // Kaydırma durduğunda ortadaki öğeyi seçili bildir.
    // isScrollInProgress, kullanıcı sürüklemesi bittikten sonra snap animasyonu
    // başlamadan önce kısa bir süre false olabilir. Bu yüzden false gördüğümüzde
    // kısa bir bekleme yapıp tekrar kontrol ediyoruz — snap animasyonu başlamışsa
    // seçim yapmıyoruz, gerçekten durmuşsa o zaman merkezdeki öğeyi buluyoruz.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    // Snap animasyonunun başlaması için kısa süre bekle
                    kotlinx.coroutines.delay(120)
                    // Hâlâ duruyorsa gerçekten yerleşmiş demektir
                    if (!listState.isScrollInProgress) {
                        val info = listState.layoutInfo
                        // item.offset, contentPadding sonrasına göre hesaplanır;
                        // viewportCenter'ı da aynı referans çerçevesine taşıyoruz.
                        val viewportCenter = info.viewportSize.height / 2 - info.beforeContentPadding
                        val centerItem = info.visibleItemsInfo.minByOrNull { item ->
                            kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                        }
                        val clamped = centerItem?.index?.coerceIn(0, items.lastIndex)
                        if (clamped != null && clamped != currentSelectedIndex) {
                            currentOnSelect(clamped)
                        }
                    }
                }
            }
    }

    Box(modifier = modifier.height(WheelItemHeight * WheelVisible)) {

        // Seçili satır vurgusu
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(WheelItemHeight)
                .background(AppPalette.Teal.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .border(1.dp, AppPalette.Teal.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
        )

        // Kaydırılabilir liste
        LazyColumn(
            state = listState,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = WheelItemHeight * half),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, text ->
                val isSelected = index == safeIndex
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize     = if (isSelected) 20.sp else 15.sp,
                        fontWeight   = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color        = if (isSelected) AppPalette.TextPrimary
                                       else AppPalette.TextSecondary.copy(alpha = 0.45f)
                    )
                }
            }
        }

        // Üst soluklaşma efekti
        Box(
            Modifier
                .fillMaxWidth()
                .height(WheelItemHeight * half)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(AppPalette.Card, Color.Transparent))
                )
        )
        // Alt soluklaşma efekti
        Box(
            Modifier
                .fillMaxWidth()
                .height(WheelItemHeight * half)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, AppPalette.Card))
                )
        )
    }
}

/**
 * Dolgulu tarih alanı.
 * Tıklanınca **Gün / Ay / Yıl** kolonlarından oluşan tekerlek seçici açılır.
 * Seçilen tarih [onDateChange] ile bildirilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonalDateField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showPicker by remember { mutableStateOf(false) }

    // Göster alanı
    TextField(
        value = formatTrDate(date),
        onValueChange = {},
        readOnly = true,
        enabled = false,          // tıklamayı dıştaki clickable yakalasın
        label = { Text(label) },
        shape = FieldShape,
        textStyle = fieldTextStyle(),
        colors = tonalFieldColors(),
        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
        modifier = modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { showPicker = true } else Modifier)
    )

    if (!showPicker) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Geçici seçim durumu — "Tamam" basılana kadar form değişmez
    var tempDay   by remember { mutableIntStateOf(date.dayOfMonth) }
    var tempMonth by remember { mutableIntStateOf(date.monthValue) }
    var tempYear  by remember { mutableIntStateOf(date.year) }

    val years = remember { (2018..2035).map { it.toString() } }

    // Ay veya yıl değişince gün üst sınırı düşebilir → sıkıştır
    LaunchedEffect(tempMonth, tempYear) {
        val max = YearMonth.of(tempYear, tempMonth).lengthOfMonth()
        if (tempDay > max) tempDay = max
    }

    // Seçilen tarihi canlı hesapla (başlık için)
    val previewDate = remember(tempYear, tempMonth, tempDay) {
        val max = YearMonth.of(tempYear, tempMonth).lengthOfMonth()
        LocalDate.of(tempYear, tempMonth, tempDay.coerceIn(1, max))
    }

    ModalBottomSheet(
        onDismissRequest = { showPicker = false },
        sheetState       = sheetState,
        containerColor   = AppPalette.Bg,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = AppPalette.Border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
        ) {
            // Başlık
            Text(
                text = "Tarih Seçin",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = AppPalette.TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            // Anlık önizleme
            Text(
                text  = formatTrDate(previewDate),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = AppPalette.Teal
            )

            Spacer(Modifier.height(24.dp))

            // Kolon başlıkları
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Gün" to 0.75f, "Ay" to 1.4f, "Yıl" to 0.9f).forEach { (title, _) ->
                    Text(
                        text = title,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        color = AppPalette.TextSecondary,
                        modifier = Modifier.weight(
                            when (title) { "Gün" -> 0.75f; "Yıl" -> 0.9f; else -> 1.4f }
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Tekerlek seçiciler
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gün
                val maxDay = YearMonth.of(tempYear, tempMonth).lengthOfMonth()
                WheelPicker(
                    items         = (1..maxDay).map { it.toString().padStart(2, '0') },
                    selectedIndex = (tempDay - 1).coerceIn(0, maxDay - 1),
                    onSelect      = { tempDay = it + 1 },
                    modifier      = Modifier.weight(0.75f)
                )

                // Ay
                WheelPicker(
                    items         = trMonths.toList(),
                    selectedIndex = tempMonth - 1,
                    onSelect      = { tempMonth = it + 1 },
                    modifier      = Modifier.weight(1.4f)
                )

                // Yıl
                val yearIndex = years.indexOf(tempYear.toString()).coerceAtLeast(0)
                WheelPicker(
                    items         = years,
                    selectedIndex = yearIndex,
                    onSelect      = { tempYear = years[it].toInt() },
                    modifier      = Modifier.weight(0.9f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Butonlar
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick  = { showPicker = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("İptal", color = AppPalette.TextSecondary, fontWeight = FontWeight.Medium)
                }
                AppButton(
                    text           = "Tamam",
                    onClick        = {
                        val max = YearMonth.of(tempYear, tempMonth).lengthOfMonth()
                        onDateChange(LocalDate.of(tempYear, tempMonth, tempDay.coerceIn(1, max)))
                        showPicker = false
                    },
                    modifier       = Modifier.weight(2f),
                    containerColor = AppPalette.Teal,
                    contentColor   = AppPalette.Bg
                )
            }
        }
    }
}
