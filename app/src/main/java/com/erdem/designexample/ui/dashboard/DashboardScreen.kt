package com.erdem.designexample.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.ui.navigation.Routes
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.LightStatusBarIcons

private val NaviBg        = AppPalette.Bg
private val NaviCard      = AppPalette.Card
private val NaviBorder    = AppPalette.Border
private val AccentTeal    = AppPalette.Teal
private val AccentAmber   = AppPalette.Amber
private val TextPrimary   = AppPalette.TextPrimary
private val TextSecondary = AppPalette.TextSecondary

/**
 * Uygulamanın ana ekranı. [DashboardViewModel]'den gelen gerçek özet verilerini gösterir:
 * en son sürüm bilgisi, kayıt meta'sı ve bekleyen ödeme özeti.
 */
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Çay Defteri",
    onSettings: () -> Unit = {},
    vm: DashboardViewModel = hiltViewModel()
) {
    val summary by vm.summary.collectAsState()
    // null = Room henüz veri göndermedi. O süreç "—" değerlerle dolu varsayılan özet göster.
    val displaySummary = summary ?: DashboardSummary()

    LightStatusBarIcons()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaviBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Üst başlık
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = onSettings,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ayarlar", tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Son sürüm özeti
        LatestSummaryCard(displaySummary)

        Spacer(Modifier.height(24.dp))

        // Büyük hero aksiyon kartları
        HeroCard(
            title     = "Yeni Kayıt",
            subtitle  = "Hasat girişi ekle ve takip et",
            icon      = Icons.Outlined.Add,
            accent    = AccentTeal,
            highlighted = false,
            onClick   = { onNavigate(Routes.RECORD_ENTRY) }
        )
        Spacer(Modifier.height(14.dp))
        HeroCard(
            title     = "Raporlar",
            subtitle  = "Özet ve grafikleri görüntüle",
            icon      = Icons.Outlined.BarChart,
            accent    = AccentAmber,
            highlighted = true,
            onClick   = { onNavigate(Routes.REPORTS) }
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "📋  Bölümler",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(14.dp))

        CollectionCard(
            title   = "Kayıtlar",
            meta    = displaySummary.recordsMeta,
            icon    = Icons.Outlined.ReceiptLong,
            onClick = { onNavigate(Routes.RECORDS) }
        )
        Spacer(Modifier.height(12.dp))
        CollectionCard(
            title   = "Ödemeler",
            meta    = displaySummary.paymentsMeta,
            icon    = Icons.Outlined.Payments,
            onClick = { onNavigate(Routes.PAYMENTS) }
        )

        Spacer(Modifier.height(24.dp))
    }
}

// --- Son sürüm kartı -------------------------------------------------------------------------

@Composable
private fun LatestSummaryCard(summary: DashboardSummary) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = NaviCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NaviBorder, RoundedCornerShape(22.dp))
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            // Başlık + dönem pili
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SON SÜRÜM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AccentTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = summary.latestSeasonLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentTeal
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            MetricRow(Icons.Outlined.Spa,      "Bu sürüm miktarı", summary.latestKg,      AccentTeal,  TextPrimary)
            ThinDivider()
            MetricRow(Icons.Outlined.Payments, "Toplam gelir",      summary.latestRevenue, AccentTeal,  AccentTeal)
            ThinDivider()
            MetricRow(Icons.Outlined.Schedule, "Bekleyen ödeme",    summary.latestPending, AccentAmber, TextPrimary)
        }
    }
}

// --- Ortak bileşenler -------------------------------------------------------------------------

@Composable
private fun MetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text  = label,
            fontSize = 15.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text  = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun ThinDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NaviBorder)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (highlighted) accent.copy(alpha = 0.7f) else NaviBorder
    val borderWidth = if (highlighted) 1.5.dp else 1.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = NaviCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 14.sp, color = TextSecondary)
            }

            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionCard(
    title: String,
    meta: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = NaviCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NaviBorder, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AccentTeal.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(text = meta, fontSize = 13.sp, color = TextSecondary)
            }

            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1621)
@Composable
private fun DashboardScreenPreview() {
    DashboardScreen(onNavigate = {})
}
