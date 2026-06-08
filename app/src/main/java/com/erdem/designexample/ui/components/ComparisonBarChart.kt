package com.erdem.designexample.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdem.designexample.ui.theme.AppPalette

/** Karşılaştırma çubuğunda tek bir kategori. [value] çubuk uzunluğuna ölçeklenir. */
data class BarEntry(
    val label: String,
    val value: Float,
    val valueText: String,
    val color: Color,
    val highlighted: Boolean = false
)

/**
 * Bağımlısız (saf Compose) **yatay** karşılaştırma çubuk grafiği. Her kategori bir satırdır:
 * etiket solda, çubuk soldan sağa en yüksek değere göre ölçeklenir, değer sağda. Kategori sayısı
 * artınca (4'ten fazla yıl/sürüm/bahçe) yalnızca aşağı doğru uzar; çubuklar incelmez, okunaklı kalır.
 * Çubuklar yumuşak bir animasyonla dolar.
 */
@Composable
fun ComparisonBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier
) {
    val maxValue = entries.maxOfOrNull { it.value }?.coerceAtLeast(0.0001f) ?: 0.0001f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        entries.forEach { entry ->
            val targetFrac = (entry.value / maxValue).coerceIn(0f, 1f)
            val frac by animateFloatAsState(
                targetValue = targetFrac,
                animationSpec = tween(durationMillis = 600),
                label = "barFraction"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sol: kategori etiketi (sabit genişlik → çubuklar hizalı başlar).
                Text(
                    text = entry.label,
                    modifier = Modifier.width(82.dp),
                    fontSize = 13.sp,
                    fontWeight = if (entry.highlighted) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (entry.highlighted) AppPalette.TextPrimary else AppPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(10.dp))

                // Orta: çubuk rayı + dolan kısım (soldan sağa).
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AppPalette.Field)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(frac)
                            .clip(RoundedCornerShape(50))
                            .background(if (entry.highlighted) entry.color else entry.color.copy(alpha = 0.55f))
                    )
                }
                Spacer(Modifier.width(10.dp))

                // Sağ: değer.
                Text(
                    text = entry.valueText,
                    modifier = Modifier.widthIn(min = 54.dp),
                    textAlign = TextAlign.End,
                    fontSize = 13.sp,
                    fontWeight = if (entry.highlighted) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (entry.highlighted) entry.color else AppPalette.TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF18222E)
@Composable
private fun ComparisonBarChartPreview() {
    ComparisonBarChart(
        entries = listOf(
            BarEntry("2022", 720f, "720", DonutPalette[0]),
            BarEntry("2023", 810f, "810", DonutPalette[1]),
            BarEntry("2024", 900f, "900", DonutPalette[2], highlighted = true),
            BarEntry("2025", 640f, "640", DonutPalette[3]),
            BarEntry("2026", 560f, "560", DonutPalette[4])
        )
    )
}
