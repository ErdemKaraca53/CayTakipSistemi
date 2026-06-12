package com.erdem.designexample.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.notifications.PaymentReminderScheduler
import com.erdem.designexample.notifications.ReminderPermissionPrefs
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
    val showErrors by vm.showErrors.collectAsState()

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    // İlk kayıttan sonra gösterilecek bildirim izni gerekçe dialog'u
    var showNotifRationale by remember { mutableStateOf(false) }
    // Eksik/geçersiz alan uyarısı — boş alan ekranın görünmeyen kısmında kalabileceğinden
    // sadece kenarlık rengiyle yetinmeyip, bildirim izni dialog'uyla aynı stilde ortada gösterilir.
    var validationMessage by remember { mutableStateOf<String?>(null) }
    // Sistem izin penceresi; sonuç ne olursa olsun ekrandan çıkılır.
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // İzin verildiyse vadesi yaklaşan ödeme varsa hemen kontrol et (anlık hatırlatma).
        if (granted) PaymentReminderScheduler.runNow(context)
        onBack()
    }

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
                // İlk (yeni) kayıttan sonra: bildirim iznini gerekçesiyle iste; aksi halde çık.
                if (!vm.isEditMode && ReminderPermissionPrefs.shouldAsk(context)) {
                    showNotifRationale = true
                } else {
                    onBack()
                }
            }
            is SaveState.Error -> {
                validationMessage = s.message
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
                        showErrors = showErrors,
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
                        showErrors = showErrors,
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

    // Eksik/geçersiz alan uyarısı — kırmızı kenarlıklı alan ekranın görünmeyen kısmında
    // kalabileceğinden, kullanıcıya bildirim izni dialog'uyla aynı stilde ortada bilgi verilir.
    validationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { validationMessage = null },
            containerColor = AppPalette.Card,
            titleContentColor = AppPalette.TextPrimary,
            textContentColor = AppPalette.TextSecondary,
            title = { Text("Eksik bilgi", fontWeight = FontWeight.SemiBold) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { validationMessage = null }) {
                    Text("Tamam", color = AppPalette.Teal, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Bildirim izni gerekçe dialog'u — neden istediğimizi açıklar, sonra sistem iznini açar.
    if (showNotifRationale) {
        AlertDialog(
            onDismissRequest = {
                ReminderPermissionPrefs.markAsked(context)
                showNotifRationale = false
                onBack()
            },
            containerColor = AppPalette.Card,
            titleContentColor = AppPalette.TextPrimary,
            textContentColor = AppPalette.TextSecondary,
            title = { Text("Ödeme hatırlatmaları", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Vadesi yaklaşan ödemelerinizi kaçırmamanız için size bildirim göndermek " +
                        "istiyoruz. Uygulamayı açmanıza gerek kalmadan, vade tarihi yaklaşınca " +
                        "hatırlatırız. Bunun için bildirim izni gerekiyor."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ReminderPermissionPrefs.markAsked(context)
                    showNotifRationale = false
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("İzin ver", color = AppPalette.Teal, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    ReminderPermissionPrefs.markAsked(context)
                    showNotifRationale = false
                    onBack()
                }) {
                    Text("Şimdi değil", color = AppPalette.TextSecondary)
                }
            }
        )
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
