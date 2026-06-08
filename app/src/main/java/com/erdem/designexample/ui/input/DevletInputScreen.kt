package com.erdem.designexample.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdem.designexample.ui.components.AppButton
import com.erdem.designexample.ui.components.FormSection
import com.erdem.designexample.ui.components.TonalAutocompleteField
import com.erdem.designexample.ui.components.TonalDateField
import com.erdem.designexample.ui.components.TonalDropdownField
import com.erdem.designexample.ui.components.TonalTextField
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.DesignExampleTheme
import java.time.LocalDate

private val seasonOptions = listOf("1. Sürüm", "2. Sürüm", "3. Sürüm", "4. Sürüm")

private val trMonths = arrayOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)
private fun formatTr(date: LocalDate) = "${date.dayOfMonth} ${trMonths[date.monthValue - 1]} ${date.year}"

/**
 * ÇAYKUR hasat girişi formu.
 *
 * Fiyat (35 TL/Kg) ve vade tarihi (satış ayının bir sonraki ayının son günü) otomatiktir;
 * kullanıcı bu iki alanı görmez/değiştiremez — ViewModel hasat tarihi değişince yeniden hesaplar.
 *
 * @param form              Anlık form state'i (ViewModel'den collectAsState ile alınır).
 * @param gardens           Bahçe adı autocomplete önerileri.
 * @param onGardenChange    Bahçe adı değişince ViewModel'e iletilir.
 * @param onHarvestDateChange Hasat tarihi değişince ViewModel'e iletilir (vade otomatik güncellenir).
 * @param onSeasonChange    Sürüm seçimi ViewModel'e iletilir.
 * @param onWeightChange    Miktar değişimi ViewModel'e iletilir.
 * @param onSave            Kaydet butonuna basılınca ViewModel.save() çağrılır.
 */
@Composable
fun DevletInputScreen(
    form: RecordFormState,
    gardens: List<String>,
    onGardenChange: (String) -> Unit,
    onHarvestDateChange: (LocalDate) -> Unit,
    onSeasonChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seasonDisplay = if (form.season in 1..4) "${form.season}. Sürüm" else ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppPalette.Bg)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Bahçe Bilgisi
        FormSection(title = "Bahçe Bilgisi", icon = Icons.Outlined.Grass) {
            TonalAutocompleteField(
                value = form.gardenName,
                onValueChange = onGardenChange,
                label = "Bahçe adı",
                suggestions = gardens,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Grass
            )
            TonalDateField(
                date = form.harvestDate,
                onDateChange = onHarvestDateChange,
                label = "Hasat tarihi"
            )
        }

        // Hasat Detayı
        FormSection(title = "Hasat Detayı", icon = Icons.Outlined.Spa) {
            TonalDropdownField(
                value = seasonDisplay,
                options = seasonOptions,
                onOptionSelected = onSeasonChange,
                label = "Sürüm",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Spa
            )
            TonalTextField(
                value = form.weightKg,
                onValueChange = onWeightChange,
                label = "Miktar (Kg)",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Scale,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }

        // Ödeme Bilgisi — otomatik hesaplanır, salt okunur
        FormSection(title = "Ödeme", icon = Icons.Outlined.Payments) {
            CaykurInfoRow(
                icon = Icons.Outlined.Payments,
                label = "Fiyat",
                value = "35 TL / Kg  (ÇAYKUR sabit fiyatı)"
            )
            CaykurInfoRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "Vade tarihi",
                value = formatTr(form.dueDate)
            )
        }

        Spacer(Modifier.height(4.dp))

        AppButton(
            text = "Kaydet",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            containerColor = AppPalette.Teal,
            contentColor = AppPalette.Bg
        )

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Salt-okunur bilgi satırı: ikon + etiket + otomatik hesaplanan değer.
 * ÇAYKUR fiyatı ve vade tarihi için kullanılır.
 */
@Composable
private fun CaykurInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppPalette.Border, RoundedCornerShape(12.dp))
            .background(AppPalette.Field, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppPalette.Teal,
            modifier = Modifier.size(20.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = AppPalette.TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppPalette.TextPrimary
            )
        }
        Text(
            text = "Otomatik",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppPalette.Teal.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DevletInputScreenPreview() {
    DesignExampleTheme {
        DevletInputScreen(
            form = RecordFormState(),
            gardens = listOf("Üst Bahçe", "Dere Kenarı"),
            onGardenChange = {},
            onHarvestDateChange = {},
            onSeasonChange = {},
            onWeightChange = {},
            onSave = {}
        )
    }
}
