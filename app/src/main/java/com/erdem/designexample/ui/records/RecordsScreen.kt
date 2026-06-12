package com.erdem.designexample.ui.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.ui.components.AppButton
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.LightStatusBarIcons
import com.erdem.designexample.ui.components.formatTrDate
import java.time.LocalDate
import java.util.Locale

/** Ekranda gösterilecek tek bir hasat kaydı (UI modeli — entity'den map edilir). */
data class RecordUi(
    val id: Long,
    val gardenName: String,
    val dateText: String,
    val seasonText: String,
    val kgText: String,
    val priceText: String,
    val totalText: String,
    val companyName: String
)

/** [HarvestEntity]'yi UI modeline dönüştürür. */
private fun HarvestEntity.toUi(): RecordUi {
    val date = LocalDate.ofEpochDay(harvestDateEpoch)
    val total = weightKg * pricePerKg
    return RecordUi(
        id = id,
        gardenName = gardenName,
        dateText = formatTrDate(date),
        seasonText = "$season. Sürüm",
        kgText = "${weightKg.toInt()} Kg",
        priceText = String.format(Locale.US, "%.1f", pricePerKg).replace('.', ',') + " TL/Kg",
        totalText = String.format(Locale.US, "%,.0f", total).replace(',', '.') + " TL",
        companyName = companyName
    )
}

private val yearOptions = listOf("Tümü", "2022", "2023", "2024", "2025")
private val seasonOptions = listOf("Tümü", "1. Sürüm", "2. Sürüm", "3. Sürüm", "4. Sürüm")

/** Silme gibi yıkıcı aksiyonlar için kırmızı vurgu. */
private val Danger = Color(0xFFE5707E)

/**
 * Kayıtlar ekranı. Ana ekran, kayıt ekleme ve raporlar ekranıyla aynı koyu navy dilini kullanır:
 * üstte iki toplam, altında okunaklı hasat kartları; filtreler "Filtrele" alt sayfasında.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordsScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: RecordsViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val rawRecords by vm.records.collectAsState()
    val gardenSuggestions by vm.gardenNames.collectAsState()

    val isLoading = rawRecords == null                       // null = henüz Room'dan veri gelmedi
    val records   = rawRecords.orEmpty().map { it.toUi() }
    val filter    = uiState.filter

    // Filtre paneli yerel state'i — Uygula'ya basılınca ViewModel'e gönderilir.
    var yearSel by remember(filter) { mutableStateOf(filter.year?.toString() ?: "Tümü") }
    var seasonSel by remember(filter) { mutableStateOf(filter.season?.let { "$it. Sürüm" } ?: "Tümü") }
    var gardenSel by remember(filter) { mutableStateOf(filter.gardenName ?: "Tümü") }

    val gardenOptions = listOf("Tümü") + gardenSuggestions

    var showFilter by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Silme sonucu snackbar
    LaunchedEffect(uiState.snackMessage) {
        uiState.snackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.consumeMessage()
        }
    }

    LightStatusBarIcons()

    // Aktif (Tümü olmayan) filtreleri özetleyen rozetler.
    val pinnedChips = buildList {
        if (yearSel != "Tümü") add(yearSel)
        if (seasonSel != "Tümü") add(seasonSel)
        if (gardenSel != "Tümü") add(gardenSel)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppPalette.Bg,
        topBar = {
            TopAppBar(
                title = { Text("Kayıtlar", fontWeight = FontWeight.SemiBold) },
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppPalette.Card,
                    contentColor = AppPalette.TextPrimary
                )
            }
        }
    ) { innerPadding ->
        // Room'dan veri henüz gelmedi: beyaz liste yerine koyu arka plan üzerinde spinner göster.
        if (isLoading) {
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Özet kartı: toplam mahsul + gelir hesaplanmış + aktif filtreler
            val totalKg      = rawRecords.orEmpty().sumOf { it.weightKg.toDouble() }.toFloat()
            val totalRevenue = rawRecords.orEmpty().sumOf { (it.weightKg * it.pricePerKg).toDouble() }.toFloat()
            item {
                NavyCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TotalTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Scale,
                                label = "Toplam Mahsul",
                                value = "${totalKg.toInt()} Kg",
                                accent = AppPalette.Teal
                            )
                            TotalTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Payments,
                                label = "Toplam Gelir",
                                value = String.format(Locale.US, "%,.0f", totalRevenue).replace(',', '.') + " TL",
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
                    }
                }
            }

            // Liste başlığı
            item {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader(icon = Icons.Outlined.Spa, title = "Hasat Kayıtları")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${records.size} kayıt",
                        fontSize = 13.sp,
                        color = AppPalette.TextSecondary
                    )
                }
            }

            items(records, key = { it.id }) { record ->
                RecordRow(
                    record = record,
                    onEdit = { onNavigateToEdit(record.id) },
                    onDelete = { vm.requestDelete(record.id) }
                )
            }
        }
    }

    if (showFilter) {
        ModalBottomSheet(
            onDismissRequest = { showFilter = false },
            sheetState = sheetState,
            containerColor = AppPalette.Bg,
            dragHandle = { BottomSheetDefaults.DragHandle(color = AppPalette.Border) }
        ) {
            RecordFilterSheet(
                yearSel = yearSel,
                onYearChange = { yearSel = it },
                seasonSel = seasonSel,
                onSeasonChange = { seasonSel = it },
                gardenSel = gardenSel,
                onGardenChange = { gardenSel = it },
                gardenOptions = gardenOptions,
                onApply = {
                    // Uygula: yerel seçimleri ViewModel'e gönder
                    vm.setYear(yearSel.toIntOrNull())
                    vm.setSeason(seasonSel.firstOrNull()?.digitToIntOrNull())
                    vm.setGarden(gardenSel.takeIf { it != "Tümü" })
                    showFilter = false
                }
            )
        }
    }

    // Silme onayı — ViewModel'den pendingDeleteId varsa dialog aç
    uiState.pendingDeleteId?.let { deleteId ->
        val rec = records.firstOrNull { it.id == deleteId }
        AlertDialog(
            onDismissRequest = { vm.cancelDelete() },
            containerColor = AppPalette.Card,
            titleContentColor = AppPalette.TextPrimary,
            textContentColor = AppPalette.TextSecondary,
            title = { Text("Kaydı sil?", fontWeight = FontWeight.SemiBold) },
            text = { Text("${rec?.gardenName ?: ""} · ${rec?.dateText ?: ""} kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                TextButton(onClick = { vm.confirmDelete() }) {
                    Text("Sil", color = Danger, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelDelete() }) {
                    Text("Vazgeç", color = AppPalette.TextSecondary)
                }
            }
        )
    }
}

// --- Kayıt kartı -----------------------------------------------------------------------------

/**
 * Tek bir hasat kaydı kartı. Üstte tarih net olarak öne çıkar; ortada bahçe + miktar/fiyat + gelir;
 * altta her kayıt için açık "Düzenle" ve "Sil" aksiyonları.
 */
@Composable
private fun RecordRow(
    record: RecordUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    NavyCard {
        Column(Modifier.padding(16.dp)) {
            // 1) Tarih başlığı (net) + sürüm pili
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppPalette.Teal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = record.dateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppPalette.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                SeasonChip(record.seasonText)
            }

            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppPalette.Border)
            )
            Spacer(Modifier.height(12.dp))

            // 2) Bahçe + miktar/fiyat (sol) ve gelir (sağ)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = record.gardenName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppPalette.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${record.kgText} · ${record.priceText}",
                        fontSize = 13.sp,
                        color = AppPalette.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = record.totalText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppPalette.Teal,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Gelir", fontSize = 11.sp, color = AppPalette.TextSecondary)
                }
            }

            // 2.1) Satış yeri (firma)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Store,
                    contentDescription = null,
                    tint = AppPalette.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Satış yeri: ${record.companyName}",
                    fontSize = 12.sp,
                    color = AppPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(14.dp))

            // 3) Aksiyonlar
            Row {
                ActionButton(
                    text = "Düzenle",
                    icon = Icons.Outlined.Edit,
                    color = AppPalette.Teal,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                ActionButton(
                    text = "Sil",
                    icon = Icons.Outlined.DeleteOutline,
                    color = Danger,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Kart altındaki ikincil aksiyon düğmesi (Düzenle / Sil). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = AppPalette.Field,
        border = BorderStroke(1.dp, AppPalette.Border),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

/** Sürüm etiketi (küçük teal pil). */
@Composable
private fun SeasonChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppPalette.Teal.copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppPalette.Teal,
            maxLines = 1,
            softWrap = false
        )
    }
}

// --- Filtre alt sayfası ----------------------------------------------------------------------

/** Kayıt filtreleme: Hangi yıl / sürüm / bahçe. Diğer ekranlarla aynı soru-temelli düzen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordFilterSheet(
    yearSel: String,
    onYearChange: (String) -> Unit,
    seasonSel: String,
    onSeasonChange: (String) -> Unit,
    gardenSel: String,
    onGardenChange: (String) -> Unit,
    gardenOptions: List<String>,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Kayıtları Filtrele",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppPalette.TextPrimary
        )

        Spacer(Modifier.height(22.dp))
        ScopeField(Icons.Outlined.CalendarMonth, "Hangi yıl?", yearOptions, yearSel, onYearChange)
        Spacer(Modifier.height(20.dp))
        ScopeField(Icons.Outlined.Spa, "Hangi sürüm?", seasonOptions, seasonSel, onSeasonChange)
        Spacer(Modifier.height(20.dp))
        ScopeField(Icons.Outlined.Grass, "Hangi bahçe?", gardenOptions, gardenSel, onGardenChange)

        Spacer(Modifier.height(28.dp))
        AppButton(
            text = "Uygula",
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            containerColor = AppPalette.Teal,
            contentColor = AppPalette.Bg
        )
        Spacer(Modifier.height(12.dp))
    }
}

/** Tek bir boyut için ikon + soru başlığı + seçilebilir çip grubu. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScopeField(
    icon: ImageVector,
    question: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppPalette.Teal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = question, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppPalette.TextPrimary)
    }
    Spacer(Modifier.height(12.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            ChoicePill(label = option, selected = option == selected, onClick = { onSelect(option) })
        }
    }
}

// --- Ortak küçük bileşenler ------------------------------------------------------------------

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
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppPalette.TextPrimary, maxLines = 1)
    }
}

/** Teal ikonlu bölüm başlığı (diğer ekranlarla aynı dil). */
@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppPalette.Teal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AppPalette.TextPrimary)
    }
}

/** Seçilebilir kapsam pili. */
@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) AppPalette.Teal else AppPalette.Field
    val textColor = if (selected) AppPalette.Bg else AppPalette.TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (selected) AppPalette.Teal else AppPalette.Border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
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
private fun RecordsScreenPreview() {
    RecordsScreen(onBack = {}, onNavigateToEdit = {})
}
