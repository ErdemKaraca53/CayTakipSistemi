package com.erdem.designexample.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.erdem.designexample.ui.components.SegmentedTabs
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.LightStatusBarIcons
import java.time.LocalDate
import java.util.Locale

// --- Veri modeli ------------------------------------------------------------------------------

/**
 * Tek bir ödeme satırı (UI modeli). [HarvestEntity]'den [toPaymentUi] ile dönüştürülür.
 * [dueDate] vade tarihi; bekleyen/alınan ayrımı [isPaid] ile belirlenir.
 */
data class PaymentUi(
    val id: Long,
    val source: String,     // "ÇAYKUR" veya özel firma adı
    val season: String,     // "2. Sürüm" gibi
    val amount: Float,      // toplam TL (weightKg × pricePerKg)
    val kgText: String,     // "120 Kg"
    val dueDate: LocalDate
)

/** [HarvestEntity] → [PaymentUi] dönüşümü. */
private fun HarvestEntity.toPaymentUi() = PaymentUi(
    id = id,
    source = if (source == "CAYKUR") "ÇAYKUR" else companyName,
    season = "$season. Sürüm",
    amount = weightKg * pricePerKg,
    kgText = "${weightKg.toInt()} Kg",
    dueDate = LocalDate.ofEpochDay(dueDateEpoch)
)

/** Vadesi bugünü geçmiş (bugün dahil) ödemeler alınmış sayılır. */
private fun PaymentUi.isPaid(ref: LocalDate) = !dueDate.isAfter(ref)

// --- Yardımcılar ------------------------------------------------------------------------------

private val trMonths = arrayOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)

private fun formatDate(d: LocalDate) = "${d.dayOfMonth} ${trMonths[d.monthValue - 1]} ${d.year}"
private fun money(v: Float) = String.format(Locale.US, "%,.0f", v).replace(',', '.') + " TL"

private val statusTabs = listOf("Tümü", "Bekleyen", "Alınan")

// --- Ekran ------------------------------------------------------------------------------------

/**
 * Ödemeler ekranı. [PaymentsViewModel]'den gelen gerçek hasat kayıtları vade tarihiyle
 * karşılaştırılarak "Bekleyen" / "Alındı" durumuna ayrılır.
 * Referans tarihi: [LocalDate.now()] — her açılışta güncellenir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: PaymentsViewModel = hiltViewModel()
) {
    val rawPayments by vm.payments.collectAsState()
    val isLoading   = rawPayments == null         // null = henüz Room'dan veri gelmedi
    val referenceDate = LocalDate.now()

    // HarvestEntity → PaymentUi dönüşümü (null güvenli)
    val payments = rawPayments.orEmpty().map { it.toPaymentUi() }

    var statusIndex by remember { mutableIntStateOf(0) }

    LightStatusBarIcons()

    val pending = payments.filter { !it.isPaid(referenceDate) }.sortedBy { it.dueDate }
    val paid    = payments.filter {  it.isPaid(referenceDate) }.sortedByDescending { it.dueDate }
    val pendingTotal = pending.sumOf { it.amount.toDouble() }.toFloat()
    val paidTotal    = paid.sumOf    { it.amount.toDouble() }.toFloat()

    val showPending  = statusIndex == 0 || statusIndex == 1
    val showPaid     = statusIndex == 0 || statusIndex == 2
    val visibleCount = (if (showPending) pending.size else 0) + (if (showPaid) paid.size else 0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppPalette.Bg,
        topBar = {
            TopAppBar(
                title = { Text("Ödemeler", fontWeight = FontWeight.SemiBold) },
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
        }
    ) { innerPadding ->
        // Room'dan veri henüz gelmedi: koyu arka plan üzerinde spinner göster.
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Özet: bekleyen + alınan toplam
            item {
                NavyCard {
                    Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TotalTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Schedule,
                            label = "Bekleyen",
                            value = money(pendingTotal),
                            accent = AppPalette.Amber
                        )
                        TotalTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.CheckCircle,
                            label = "Alınan",
                            value = money(paidTotal),
                            accent = AppPalette.Teal
                        )
                    }
                }
            }

            // Durum sekmesi
            item {
                SegmentedTabs(
                    options = statusTabs,
                    selectedIndex = statusIndex,
                    onSelect = { statusIndex = it }
                )
            }

            // Bekleyen bölümü
            if (showPending && pending.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(2.dp))
                    SectionHeader(Icons.Outlined.Schedule, "Bekleyen Ödemeler", AppPalette.Amber, "${pending.size}")
                }
                items(pending, key = { it.id }) { p -> PaymentRow(p, isPaid = false) }
            }

            // Gerçekleşen bölümü
            if (showPaid && paid.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(2.dp))
                    SectionHeader(Icons.Outlined.CheckCircle, "Gerçekleşen Ödemeler", AppPalette.Teal, "${paid.size}")
                }
                items(paid, key = { it.id }) { p -> PaymentRow(p, isPaid = true) }
            }

            // Boş durum
            if (visibleCount == 0) {
                item { EmptyNote() }
            }
        }
    }
}

// --- Ödeme kartı -----------------------------------------------------------------------------

/** Tek bir ödeme kartı: kaynak · sürüm · vade tarihi · tutar · durum rozeti. */
@Composable
private fun PaymentRow(p: PaymentUi, isPaid: Boolean) {
    val color = if (isPaid) AppPalette.Teal else AppPalette.Amber
    val statusLabel = if (isPaid) "Alındı" else "Bekliyor"
    val statusIcon  = if (isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule

    NavyCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: durum ikonu
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(14.dp))

            // Orta: kaynak · sürüm + vade tarihi
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${p.source} · ${p.season}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppPalette.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = AppPalette.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Vade: ${formatDate(p.dueDate)}",
                        fontSize = 13.sp,
                        color = AppPalette.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Sağ: tutar + durum rozeti
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = money(p.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(statusLabel, color)
            }
        }
    }
}

/** Durum pili (Alındı / Bekliyor). */
@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

/** Filtre sonucu boşsa gösterilen not. */
@Composable
private fun EmptyNote() {
    NavyCard {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Bu durumda ödeme yok.", fontSize = 14.sp, color = AppPalette.TextSecondary)
        }
    }
}

// --- Ortak küçük bileşenler ------------------------------------------------------------------

/** Özet kutusu (ikon + etiket + değer). */
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

/** İkonlu bölüm başlığı + sağda adet. */
@Composable
private fun SectionHeader(icon: ImageVector, title: String, tint: Color, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppPalette.TextPrimary
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(
                text = trailing,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppPalette.TextSecondary
            )
        }
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
private fun PaymentsScreenPreview() {
    // Preview ViewModel olmadan çalışmaz; boş liste ile tasarımı doğrulamak için
    // örnek veri doğrudan kullanılabilir.
    PaymentsScreen(onBack = {})
}
