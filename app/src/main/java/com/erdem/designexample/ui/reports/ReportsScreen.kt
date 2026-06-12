package com.erdem.designexample.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.ui.components.AppButton
import com.erdem.designexample.ui.components.BarEntry
import com.erdem.designexample.ui.components.ComparisonBarChart
import com.erdem.designexample.ui.components.DonutPalette
import com.erdem.designexample.ui.components.WheelPicker
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.LightStatusBarIcons
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// --- Sabitler ---------------------------------------------------------------------------------

private const val ALL_GARDENS = "Tüm Bahçeler"
private const val ALL_YEARS   = "Tüm Yıllar"
private const val ALL_SEASONS = "Tüm Sürümler"

/** Sürüm seçenekleri her zaman sabit 4 değer. */
private val seasonOptions = listOf(ALL_SEASONS, "1. Sürüm", "2. Sürüm", "3. Sürüm", "4. Sürüm")

private val Positive = AppPalette.Teal
private val Negative = Color(0xFFE5707E)

// --- Veri modeli ------------------------------------------------------------------------------

/** Kırılım ekseni (neye göre karşılaştırılacağı). */
private enum class CompareMode { YEAR, SEASON, GARDEN }

/** Grafik ölçütü: çubukları Kg mi TL mi temsil eder. */
private enum class Metric(val label: String, val unit: String) {
    KG("Miktar", "Kg"),
    REVENUE("Gelir", "TL")
}

/** Grafik çubuğu verisi: hem kg hem gelir taşır; [value] ölçüte göre seçilir. */
private data class Bar(val label: String, val kg: Float, val revenue: Float) {
    fun value(m: Metric) = if (m == Metric.KG) kg else revenue
}

// --- Aggregasyon (gerçek DB verisi) -----------------------------------------------------------

/**
 * Verilen seçimlere göre kayıtları filtreler ve toplar.
 * "Tümü" değerleri tüm boyutu kapsar.
 */
private fun aggregateReal(
    records: List<HarvestEntity>,
    yearSel: String,
    seasonSel: String,
    gardenSel: String
): Bar {
    val filtered = records.filter { e ->
        val year   = LocalDate.ofEpochDay(e.harvestDateEpoch).year.toString()
        val season = "${e.season}. Sürüm"
        (yearSel   == ALL_YEARS   || year   == yearSel) &&
        (seasonSel == ALL_SEASONS || season == seasonSel) &&
        (gardenSel == ALL_GARDENS || e.gardenName == gardenSel)
    }
    val kg      = filtered.sumOf { it.weightKg.toDouble() }.toFloat()
    val revenue = filtered.sumOf { (it.weightKg * it.pricePerKg).toDouble() }.toFloat()
    return Bar("", kg, revenue)
}

/**
 * Kırılım eksenine göre bar serisi üretir.
 * - [CompareMode.YEAR]   → verideki her yıl bir çubuk.
 * - [CompareMode.SEASON] → 1–4 sürüm (veri olan sürümler).
 * - [CompareMode.GARDEN] → verideki her benzersiz bahçe bir çubuk.
 */
private fun buildSeriesReal(
    records: List<HarvestEntity>,
    axis: CompareMode,
    yearSel: String,
    seasonSel: String,
    gardenSel: String
): List<Bar> = when (axis) {
    CompareMode.YEAR -> {
        records
            .map { LocalDate.ofEpochDay(it.harvestDateEpoch).year }
            .distinct().sorted()
            .map { y -> aggregateReal(records, y.toString(), seasonSel, gardenSel).copy(label = y.toString()) }
    }
    CompareMode.SEASON -> {
        (1..4)
            .map { s -> "$s. Sürüm" }
            .map { sLabel -> aggregateReal(records, yearSel, sLabel, gardenSel).copy(label = sLabel) }
            .filter { it.kg > 0f }  // verisi olmayan sürümleri gizle
    }
    CompareMode.GARDEN -> {
        records.map { it.gardenName }.distinct().sorted()
            .map { g -> aggregateReal(records, yearSel, seasonSel, g).copy(label = g) }
    }
}

// --- Yardımcı fonksiyonlar -------------------------------------------------------------------

private fun groupInt(v: Float): String =
    String.format(Locale.US, "%,d", v.roundToInt()).replace(',', '.')

private fun avgPriceText(kg: Float, revenue: Float): String {
    if (kg <= 0f) return "—"
    return String.format(Locale.US, "%.1f", revenue / kg).replace('.', ',') + " TL/Kg"
}

// --- Ekran ------------------------------------------------------------------------------------

/**
 * Raporlar ekranı. [ReportsViewModel]'den gelen gerçek hasat kayıtlarını kullanır.
 *
 * Filtre mantığı:
 *  - Hangi yıl / sürüm / bahçe seçilir (her biri "Tümü" ya da spesifik).
 *  - "Tümü" bırakılan boyut sayısı = 0 → spesifik dönem detayı (ortalama fiyat + toplam).
 *  - "Tümü" bırakılan boyut sayısı > 0 → karşılaştırma modu (çubuk grafik + detay liste).
 *  - Birden fazla "Tümü" varsa kırılım ekseni kullanıcı tarafından seçilir.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ReportsViewModel = hiltViewModel()
) {
    val allHarvests by vm.allHarvests.collectAsState()
    val gardenNames by vm.gardenNames.collectAsState()

    // Filtre seçimleri (yerel UI state)
    var yearSel      by remember { mutableStateOf(ALL_YEARS) }
    var seasonSel    by remember { mutableStateOf(ALL_SEASONS) }
    var gardenSel    by remember { mutableStateOf(ALL_GARDENS) }
    var metric       by remember { mutableStateOf(Metric.KG) }
    var groupOverride by remember { mutableStateOf<CompareMode?>(null) }

    var showFilter by remember { mutableStateOf(false) }

    LightStatusBarIcons()

    // Dinamik seçenek listeleri — canlı DB verisinden türetilir (null güvenli)
    val availableYears = allHarvests.orEmpty()
        .map { LocalDate.ofEpochDay(it.harvestDateEpoch).year }
        .distinct().sorted().map { it.toString() }
    val yearOptions   = listOf(ALL_YEARS)   + availableYears
    val gardenOptions = listOf(ALL_GARDENS) + gardenNames

    // "Tümü" bırakılan boyutlar → karşılaştırma eksenleri (öncelik: Yıl → Sürüm → Bahçe)
    val freeDims = buildList {
        if (yearSel   == ALL_YEARS)   add(CompareMode.YEAR)
        if (seasonSel == ALL_SEASONS) add(CompareMode.SEASON)
        if (gardenSel == ALL_GARDENS) add(CompareMode.GARDEN)
    }
    val groupDim: CompareMode? = when {
        freeDims.isEmpty()                              -> null
        groupOverride != null && groupOverride in freeDims -> groupOverride
        else                                            -> freeDims.first()
    }

    // Seçili kapsamın toplamı (her iki modda da geçerli)
    val totalCell = aggregateReal(allHarvests.orEmpty(), yearSel, seasonSel, gardenSel)

    // Aktif seçim rozetleri
    val pinnedChips = buildList {
        if (yearSel   != ALL_YEARS)   add(yearSel)
        if (seasonSel != ALL_SEASONS) add(seasonSel)
        if (gardenSel != ALL_GARDENS) add(gardenSel)
    }

    val headerTitle = when (groupDim) {
        CompareMode.YEAR   -> "Yıl Karşılaştırması"
        CompareMode.SEASON -> "Sürüm Karşılaştırması"
        CompareMode.GARDEN -> "Bahçe Karşılaştırması"
        null               -> "Seçilen Dönem"
    }
    val headerIcon = when (groupDim) {
        CompareMode.YEAR   -> Icons.Outlined.CalendarMonth
        CompareMode.SEASON -> Icons.Outlined.Spa
        CompareMode.GARDEN -> Icons.Outlined.Grass
        null               -> Icons.Outlined.Insights
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppPalette.Bg,
        topBar = {
            TopAppBar(
                title = { Text("Raporlar", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPalette.Bg,
                    titleContentColor = AppPalette.TextPrimary,
                    navigationIconContentColor = AppPalette.TextPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showFilter = true },
                icon = { Icon(Icons.Outlined.Tune, contentDescription = null) },
                text = { Text("Filtrele", fontWeight = FontWeight.SemiBold) },
                containerColor = AppPalette.Teal,
                contentColor = AppPalette.Bg
            )
        }
    ) { innerPadding ->

        // Room'dan veri henüz gelmedi: spinner göster (beyaz liste yerine).
        if (allHarvests == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPalette.Teal, strokeWidth = 3.dp)
            }
            return@Scaffold
        }

        // DB boş: kullanıcıyı yönlendir.
        if (allHarvests!!.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz kayıt yok.\nYeni Kayıt ekranından hasat ekleyebilirsiniz.",
                    fontSize = 14.sp,
                    color = AppPalette.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Başlık
            item { SectionHeader(icon = headerIcon, title = headerTitle) }

            // Özet kartı
            item {
                NavyCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TotalTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Scale,
                                label = "Toplam Mahsul",
                                value = "${groupInt(totalCell.kg)} Kg",
                                accent = AppPalette.Teal
                            )
                            TotalTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Payments,
                                label = "Toplam Gelir",
                                value = "${groupInt(totalCell.revenue)} TL",
                                accent = AppPalette.Amber
                            )
                        }

                        if (pinnedChips.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pinnedChips.forEach { ReadonlyChip(it) }
                            }
                        }

                        if (groupDim == null) {
                            // Spesifik dönem: ortalama fiyat satırı
                            Spacer(Modifier.height(16.dp))
                            ThinDivider()
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ortalama fiyat",
                                    fontSize = 14.sp,
                                    color = AppPalette.TextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = avgPriceText(totalCell.kg, totalCell.revenue),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppPalette.Teal
                                )
                            }
                        } else {
                            // Karşılaştırma: çubuk grafik
                            val series = buildSeriesReal(allHarvests.orEmpty(), groupDim, yearSel, seasonSel, gardenSel)
                            val maxValue = series.maxOfOrNull { it.value(metric) } ?: 0f
                            Spacer(Modifier.height(20.dp))
                            ComparisonBarChart(
                                entries = series.mapIndexed { index, b ->
                                    BarEntry(
                                        label = b.label,
                                        value = b.value(metric),
                                        valueText = groupInt(b.value(metric)),
                                        color = DonutPalette[index % DonutPalette.size],
                                        highlighted = b.value(metric) == maxValue
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Karşılaştırma modunda detay liste
            if (groupDim != null) {
                val series = buildSeriesReal(allHarvests.orEmpty(), groupDim, yearSel, seasonSel, gardenSel)
                val maxValue = series.maxOfOrNull { it.value(metric) } ?: 0f
                val total = series.sumOf { it.value(metric).toDouble() }.toFloat()

                item {
                    Spacer(Modifier.height(2.dp))
                    SectionHeader(icon = headerIcon, title = "Detaylı Karşılaştırma")
                }
                itemsIndexed(series) { index, b ->
                    val accent = DonutPalette[index % DonutPalette.size]
                    val barFraction = if (groupDim == CompareMode.YEAR) {
                        if (maxValue > 0f) b.value(metric) / maxValue else 0f
                    } else {
                        if (total > 0f) b.value(metric) / total else 0f
                    }
                    CompareRow(
                        color = accent,
                        label = b.label,
                        kgText = "${groupInt(b.kg)} Kg",
                        revenueText = "${groupInt(b.revenue)} TL",
                        barFraction = barFraction
                    ) {
                        if (groupDim == CompareMode.YEAR) {
                            DeltaBadge(
                                current = b.value(metric),
                                previous = series.getOrNull(index - 1)?.value(metric)
                            )
                        } else {
                            val pct = if (total > 0f) (b.value(metric) / total * 100).roundToInt() else 0
                            Text(
                                text = "Payı %$pct",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = accent
                            )
                        }
                    }
                }
            }
        }
    }

    // Filtre — ekranın ortasında açılan dialog (tarih seçici tarzı tekerlekler)
    if (showFilter) {
        ReportFilterDialog(
            yearOptions   = yearOptions,
            gardenOptions = gardenOptions,
            yearSel       = yearSel,
            seasonSel     = seasonSel,
            gardenSel     = gardenSel,
            onApply       = { y, s, g ->
                yearSel = y; seasonSel = s; gardenSel = g
                showFilter = false
            },
            onDismiss     = { showFilter = false }
        )
    }
}

// --- Filtre dialog'u --------------------------------------------------------------------------

/**
 * Rapor filtresi — ekranın **ortasında** açılan dialog (bottom sheet değil).
 * Tarih seçicideki gibi üç tekerlek: **Yıl · Bahçe · Sürüm**. Seçim "Uygula" ile
 * uygulanır; "İptal" değişiklikleri yok sayar. Yıl/bahçe seçenekleri DB'den canlı gelir.
 */
@Composable
private fun ReportFilterDialog(
    yearOptions: List<String>,
    gardenOptions: List<String>,
    yearSel: String,
    seasonSel: String,
    gardenSel: String,
    onApply: (year: String, season: String, garden: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Geçici seçim — "Uygula" basılana kadar rapor değişmez (tarih seçici davranışı).
    var tempYear   by remember { mutableStateOf(yearSel) }
    var tempGarden by remember { mutableStateOf(gardenSel) }
    var tempSeason by remember { mutableStateOf(seasonSel) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppPalette.Card,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Rapor Filtrele",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppPalette.TextPrimary
                )

                Spacer(Modifier.height(20.dp))

                // Kolon başlıkları
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Yıl" to 1f, "Bahçe" to 1.4f, "Sürüm" to 1.1f).forEach { (title, w) ->
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = AppPalette.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(w)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Üç tekerlek — ortadaki öğe seçili
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        items         = yearOptions,
                        selectedIndex = yearOptions.indexOf(tempYear).coerceAtLeast(0),
                        onSelect      = { tempYear = yearOptions[it] },
                        modifier      = Modifier.weight(1f)
                    )
                    WheelPicker(
                        items         = gardenOptions,
                        selectedIndex = gardenOptions.indexOf(tempGarden).coerceAtLeast(0),
                        onSelect      = { tempGarden = gardenOptions[it] },
                        modifier      = Modifier.weight(1.4f)
                    )
                    WheelPicker(
                        items         = seasonOptions,
                        selectedIndex = seasonOptions.indexOf(tempSeason).coerceAtLeast(0),
                        onSelect      = { tempSeason = seasonOptions[it] },
                        modifier      = Modifier.weight(1.1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Butonlar
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("İptal", color = AppPalette.TextSecondary, fontWeight = FontWeight.Medium)
                    }
                    AppButton(
                        text           = "Uygula",
                        onClick        = { onApply(tempYear, tempSeason, tempGarden) },
                        modifier       = Modifier.weight(2f),
                        containerColor = AppPalette.Teal,
                        contentColor   = AppPalette.Bg
                    )
                }
            }
        }
    }
}

// --- Liste & yardımcı bileşenler -------------------------------------------------------------

/** Özet kartındaki tek toplam kutusu (ikon + etiket + değer). */
@Composable
private fun TotalTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = AppPalette.TextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppPalette.TextPrimary,
            maxLines = 1
        )
    }
}

/**
 * Detay listesindeki tek karşılaştırma satırı. Her zaman hem mahsul (Kg) hem gelir (TL);
 * çubuk seçili ölçüte göre dolar, trailing rozet pay/değişimi gösterir.
 */
@Composable
private fun CompareRow(
    color: Color,
    label: String,
    kgText: String,
    revenueText: String,
    barFraction: Float,
    trailing: @Composable () -> Unit
) {
    NavyCard {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppPalette.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                trailing()
            }
            Spacer(Modifier.height(12.dp))
            ShareBar(fraction = barFraction, color = color)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ValueCell(label = "Mahsul", value = kgText,      modifier = Modifier.weight(1f), alignEnd = false)
                ValueCell(label = "Gelir",  value = revenueText, modifier = Modifier.weight(1f), alignEnd = true)
            }
        }
    }
}

/** Etiketli tek değer hücresi (Mahsul / Gelir). */
@Composable
private fun ValueCell(label: String, value: String, modifier: Modifier = Modifier, alignEnd: Boolean) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label.uppercase(Locale("tr")),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = AppPalette.TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppPalette.TextPrimary,
            maxLines = 1
        )
    }
}

/** Önceki döneme göre yüzde değişim rozeti. */
@Composable
private fun DeltaBadge(current: Float, previous: Float?) {
    if (previous == null || previous == 0f) {
        Text(text = "—", fontSize = 13.sp, color = AppPalette.TextSecondary)
        return
    }
    val pct = ((current - previous) / previous * 100).roundToInt()
    val up  = pct >= 0
    Text(
        text = "${if (up) "▲" else "▼"} %${abs(pct)}",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (up) Positive else Negative
    )
}

/** Görsel pay/oran çubuğu. */
@Composable
private fun ShareBar(fraction: Float, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(AppPalette.Field)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** Teal ikonlu bölüm başlığı. */
@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppPalette.Teal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AppPalette.TextPrimary)
    }
}

/** Salt-okunur kapsam rozeti. */
@Composable
private fun ReadonlyChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppPalette.Field)
            .border(1.dp, AppPalette.Border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppPalette.TextSecondary)
    }
}

/** İnce tam genişlik ayraç. */
@Composable
private fun ThinDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppPalette.Border)
    )
}

/** Navy temaya uygun kart kabuğu. */
@Composable
private fun NavyCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppPalette.Card,
        border = BorderStroke(1.dp, AppPalette.Border)
    ) {
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1621)
@Composable
private fun ReportsScreenPreview() {
    ReportsScreen(onBack = {})
}
