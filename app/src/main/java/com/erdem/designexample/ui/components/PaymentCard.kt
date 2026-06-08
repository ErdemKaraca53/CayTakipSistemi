package com.erdem.designexample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdem.designexample.ui.theme.DesignExampleTheme

/** Ödeme kartının durumu (vade gelmedi / ödendi). */
enum class PaymentStatus { PENDING, PAID }

/**
 * Tek bir ödemeyi gösteren kart: firma, tutar, vade tarihi, kalan gün ve durum rozeti.
 */
@Composable
fun PaymentCard(
    company: String,
    amountText: String,
    dueDateText: String,
    remainingText: String,
    kgText: String,
    priceText: String,
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val isPending = status == PaymentStatus.PENDING
    val statusColor = if (isPending) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val statusLabel = if (isPending) "Ödeme bekliyor" else "Ödendi"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = company,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dueDateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(label = statusLabel, color = statusColor)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isPending) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = kgText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentCardPreview() {
    DesignExampleTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentCard(
                company = "ÇAYKUR",
                amountText = "1.860 TL",
                dueDateText = "30 Haziran 2024",
                remainingText = "12 gün kaldı",
                kgText = "120 Kg",
                priceText = "15,5 TL",
                status = PaymentStatus.PENDING
            )
            PaymentCard(
                company = "Yeşil Çay Ltd.",
                amountText = "1.360 TL",
                dueDateText = "2 Mart 2024",
                remainingText = "",
                kgText = "85 Kg",
                priceText = "16,0 TL",
                status = PaymentStatus.PAID
            )
        }
    }
}
