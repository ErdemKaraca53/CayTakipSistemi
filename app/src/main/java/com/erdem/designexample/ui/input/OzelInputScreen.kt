package com.erdem.designexample.ui.input

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

/**
 * Özel alıcı hasat girişi formu.
 * Tüm state [RecordEntryViewModel]'den gelir.
 */
@Composable
fun OzelInputScreen(
    form: RecordFormState,
    gardens: List<String>,
    sellers: List<String>,
    onGardenChange: (String) -> Unit,
    onCompanyChange: (String) -> Unit,
    onHarvestDateChange: (LocalDate) -> Unit,
    onSeasonChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDueDateChange: (LocalDate) -> Unit,
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
        FormSection(title = "Bahçe & Alıcı", icon = Icons.Outlined.Grass) {
            TonalAutocompleteField(
                value = form.gardenName,
                onValueChange = onGardenChange,
                label = "Bahçe adı",
                suggestions = gardens,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Grass
            )
            TonalAutocompleteField(
                value = form.companyName,
                onValueChange = onCompanyChange,
                label = "Satış yeri (firma)",
                suggestions = sellers,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Store
            )
        }

        FormSection(title = "Hasat Detayı", icon = Icons.Outlined.Spa) {
            TonalDateField(
                date = form.harvestDate,
                onDateChange = onHarvestDateChange,
                label = "Hasat tarihi"
            )
            TonalDropdownField(
                value = seasonDisplay,
                options = seasonOptions,
                onOptionSelected = onSeasonChange,
                label = "Sürüm",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Spa
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TonalTextField(
                    value = form.weightKg,
                    onValueChange = onWeightChange,
                    label = "Miktar (Kg)",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.Scale,
                    keyboardType = KeyboardType.Number
                )
                TonalTextField(
                    value = form.pricePerKg,
                    onValueChange = onPriceChange,
                    label = "Fiyat (TL/Kg)",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.Payments,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            }
        }

        FormSection(title = "Ödeme", icon = Icons.Outlined.Payments) {
            TonalDateField(
                date = form.dueDate,
                onDateChange = onDueDateChange,
                label = "Vade tarihi"
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

@Preview(showBackground = true)
@Composable
private fun OzelInputScreenPreview() {
    DesignExampleTheme {
        OzelInputScreen(
            form = RecordFormState(source = RecordSource.OZEL),
            gardens = listOf("Üst Bahçe"),
            sellers = listOf("Yeşil Çay Ltd."),
            onGardenChange = {},
            onCompanyChange = {},
            onHarvestDateChange = {},
            onSeasonChange = {},
            onWeightChange = {},
            onPriceChange = {},
            onDueDateChange = {},
            onSave = {}
        )
    }
}
