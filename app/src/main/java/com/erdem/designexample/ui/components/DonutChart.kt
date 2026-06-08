package com.erdem.designexample.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdem.designexample.ui.theme.AppPalette
import kotlin.math.roundToInt

/** Donut chart için tek bir dilim. [value] orana, [valueText] lejanttaki gösterime girer. */
data class DonutSlice(val label: String, val value: Float, val color: Color, val valueText: String = "")

/** Koyu navy zeminde okunaklı, canlı donut renkleri. */
val DonutPalette = listOf(
    Color(0xFF17B9A6), // teal
    Color(0xFFE7A33C), // amber
    Color(0xFF5B8DEF), // mavi
    Color(0xFFB57EDC), // mor
    Color(0xFFE5707E)  // gül
)

/**
 * Bağımlılıksız (saf Compose Canvas) donut grafik + sağında zengin lejant (etiket, yüzde, değer).
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
            Canvas(modifier = Modifier.size(148.dp)) {
                val strokeWidth = size.minDimension * 0.15f
                val inset = strokeWidth / 2
                // Arka plan halkası (boş kısımların belli olması için)
                drawArc(
                    color = AppPalette.Field,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth)
                )
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = (slice.value / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep - 3f, // dilimler arası küçük boşluk
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerValue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPalette.TextPrimary
                )
                Text(
                    text = centerLabel,
                    fontSize = 13.sp,
                    color = AppPalette.TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            slices.forEach { slice ->
                val pct = (slice.value / total * 100).roundToInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(11.dp).background(slice.color, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = slice.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppPalette.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (slice.valueText.isNotEmpty()) {
                            Text(
                                text = slice.valueText,
                                fontSize = 12.sp,
                                color = AppPalette.TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = "%$pct",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = slice.color
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF18222E)
@Composable
private fun DonutChartPreview() {
    DonutChart(
        slices = listOf(
            DonutSlice("1. Sürüm", 320f, DonutPalette[0], "320 Kg"),
            DonutSlice("2. Sürüm", 280f, DonutPalette[1], "280 Kg"),
            DonutSlice("3. Sürüm", 210f, DonutPalette[2], "210 Kg"),
            DonutSlice("4. Sürüm", 90f, DonutPalette[3], "90 Kg")
        ),
        centerLabel = "Toplam",
        centerValue = "900 Kg",
        modifier = Modifier.padding(16.dp)
    )
}
