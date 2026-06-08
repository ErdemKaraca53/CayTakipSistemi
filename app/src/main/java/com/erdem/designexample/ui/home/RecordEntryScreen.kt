package com.erdem.designexample.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.ui.components.SegmentedTabs
import com.erdem.designexample.ui.input.DevletInputScreen
import com.erdem.designexample.ui.input.OzelInputScreen
import com.erdem.designexample.ui.input.RecordEntryViewModel
import com.erdem.designexample.ui.input.RecordFormState
import com.erdem.designexample.ui.input.RecordSource
import com.erdem.designexample.ui.input.SaveState
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.DesignExampleTheme
import com.erdem.designexample.ui.theme.LightStatusBarIcons
import kotlinx.coroutines.launch

private val tabs = listOf("ÇAYKUR", "Özel")

/**
 * Yeni Kayıt ekranı. [RecordEntryViewModel] ile bağlı: form state ViewModel'de tutulur,
 * kaydet başarılı olunca ekrandan çıkılır, hata Snackbar ile gösterilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEntryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: RecordEntryViewModel = hiltViewModel()
) {
    val form by vm.formState.collectAsState()
    val saveState by vm.saveState.collectAsState()
    val gardens by vm.gardenNames.collectAsState()
    val sellers by vm.sellerNames.collectAsState()

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Kullanıcı sekme değişince (kaydırma / tıklama) → ViewModel'e source güncelle.
    // setSource() içinde kaynak zaten aynıysa erken dönüş var, companyName ezilmez.
    LaunchedEffect(pagerState.currentPage) {
        vm.setSource(if (pagerState.currentPage == 0) RecordSource.CAYKUR else RecordSource.OZEL)
    }

    // Edit modunda loadForEdit() tamamlanınca form.source değişir → pager'ı doğru sekmeye taşı.
    // scrollToPage: animasyonsuz, anlık — yükleme sırasında titreme olmasın.
    LaunchedEffect(form.source) {
        val target = if (form.source == RecordSource.OZEL) 1 else 0
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    // Kaydet sonuçlarını dinle
    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Success -> {
                vm.consumeSaveState()
                onBack()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                vm.consumeSaveState()
            }
            else -> Unit
        }
    }

    LightStatusBarIcons()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPalette.Bg)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = if (vm.isEditMode) "Kaydı Düzenle" else "Yeni Kayıt",
                        fontWeight = FontWeight.SemiBold
                    )
                },
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

            SegmentedTabs(
                options = tabs,
                selectedIndex = pagerState.currentPage,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DevletInputScreen(
                        form = form,
                        gardens = gardens,
                        onGardenChange = vm::setGardenName,
                        onHarvestDateChange = vm::setHarvestDate,
                        onSeasonChange = vm::setSeason,
                        onWeightChange = vm::setWeightKg,
                        // Fiyat (35 TL) ve vade tarihi ViewModel tarafından otomatik yönetilir
                        onSave = vm::save
                    )
                    else -> OzelInputScreen(
                        form = form,
                        gardens = gardens,
                        sellers = sellers,
                        onGardenChange = vm::setGardenName,
                        onCompanyChange = vm::setCompanyName,
                        onHarvestDateChange = vm::setHarvestDate,
                        onSeasonChange = vm::setSeason,
                        onWeightChange = vm::setWeightKg,
                        onPriceChange = vm::setPricePerKg,
                        onDueDateChange = vm::setDueDate,
                        onSave = vm::save
                    )
                }
            }
        }

        // Yükleniyor göstergesi
        if (saveState is SaveState.Loading) {
            CircularProgressIndicator(
                color = AppPalette.Teal,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Hata snackbar'ı
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppPalette.Card,
                contentColor = AppPalette.TextPrimary,
                actionColor = AppPalette.Teal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordEntryScreenPreview() {
    DesignExampleTheme {
        // Preview için ViewModel olmadan doğrudan composable kullan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPalette.Bg)
        ) {
            DevletInputScreen(
                form = RecordFormState(),
                gardens = listOf("Üst Bahçe"),
                onGardenChange = {},
                onHarvestDateChange = {},
                onSeasonChange = {},
                onWeightChange = {},
                onSave = {}
            )
        }
    }
}
