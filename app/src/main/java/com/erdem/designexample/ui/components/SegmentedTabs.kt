package com.erdem.designexample.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.erdem.designexample.ui.theme.AppPalette

/**
 * Modern, animasyonlu segment kontrolü (örn. ÇAYKUR | Özel). TabRow yerine daha şık bir alternatif:
 * seçili segment yumuşak geçişle vurgulanır.
 */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = AppPalette.Card
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(4.dp)
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val bg by animateColorAsState(
                    if (selected) AppPalette.Teal else Color.Transparent,
                    label = "segmentBg"
                )
                val textColor by animateColorAsState(
                    if (selected) AppPalette.Bg else AppPalette.TextSecondary,
                    label = "segmentText"
                )
                val elevation by animateDpAsState(if (selected) 3.dp else 0.dp, label = "segmentElev")

                Surface(
                    color = bg,
                    shape = RoundedCornerShape(50),
                    shadowElevation = elevation,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .selectable(selected = selected, onClick = { onSelect(index) })
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
