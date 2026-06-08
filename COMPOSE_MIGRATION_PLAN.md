# Jetpack Compose Migration Planı

> Çay bahçesi hasat & ödeme takip uygulamasının XML → Jetpack Compose + Material 3 dönüşümü.
> Strateji: **Ekran ekran, hibrit mod**. Eski sürüm test edilmeden silinmez.

---

## 1. Mevcut Durum (Analiz)

### Uygulama
Çay bahçelerinden yapılan hasatların (kg), satış yeri (ÇAYKUR / Özel alıcı), satış fiyatı ve
vade tarihlerinin takip edildiği bir uygulama. Veriler SQLite'ta tutulur, Firebase Firestore'a yedeklenir.

### Teknik Yığın

| Katman | Şu an | Hedef |
|--------|-------|-------|
| UI | XML + ViewBinding (19 layout) | Compose + Material 3 |
| Navigation | Navigation Component (XML graph) | Navigation-Compose (type-safe) |
| Ekran | Single Activity + 9 Fragment | Single Activity + Composable screens |
| State | `LiveData` (3 ViewModel) | `StateFlow` |
| DI | Yok (manuel) | Hilt |
| Veri | Raw SQLite (`DatabaseHelper` / `DatabaseOperations`) | Repository (sonra Room — opsiyonel) |
| Listeler | 5x RecyclerView + Adapter | `LazyColumn` |
| Grafik | MPAndroidChart | `AndroidView` köprü → sonra Compose Canvas |
| Auth / Sync | Firebase Auth + Firestore | Aynen korunur |
| Compose | **Yok** (gradle'da bile yok) | Eklenecek |

### Ekran Envanteri

| # | Fragment / Sınıf | Layout | Satır | İşlev |
|---|------------------|--------|------:|-------|
| 1 | `log_in/sign_up.kt` (SignUp) | `fragment_sign_up.xml` | 259 | Kayıt ol (başlangıç ekranı) |
| 2 | `log_in/login.kt` (Login) | `fragment_login.xml` | 141 | Giriş yap |
| 3 | `New_design.kt` (new_design) | `fragment_new_design.xml` | 78 | Ana ekran — TabLayout + ViewPager (ÇAYKUR/Özel) |
| 4 | `input/DevletFragment.kt` | `fragment_devlet.xml` | 164 | ÇAYKUR hasat girişi |
| 5 | `input/OzelFragment.kt` | `fragment_ozel.xml` | 273 | Özel alıcı hasat girişi |
| 6 | `kayitSayfa/KayitEkranFragment.kt` | `fragment_kayit_sayfasi.xml` | 124 | Kayıt listesi |
| 7 | `kayitSayfa/KayitFilterDialogFragment.kt` | `fragment_kayit_filter...xml` | 136 | Kayıt filtre BottomSheet |
| 8 | `odemelerSayfasi/OdemeSayfasiFragment.kt` | `fragment_odeme_sayfasi.xml` | 100 | Ödemeler listesi |
| 9 | `odemelerSayfasi/filterListDialogFragment.kt` | `filter_model_bottom_sheet2.xml` | 180 | Ödeme filtre BottomSheet |
| 10 | `ikinciFragment.kt` | `fragment_ikinici.xml` | 324 | Bahçe rapor + pie chart |
| 11 | `birinciFragment` | `fragment_birinci.xml` | — | Grafik/genel rapor |
| 12 | `bahceRapor.kt` | — | 66 | Bahçe raporu |
| 13 | `ItemListDialogFragment.kt` | `fragment_item_list_dialog...xml` | 103 | Liste seçim dialog |

**Adapter'lar (→ LazyColumn item'larına dönüşecek):** `BahceAdapter`, `GrafikAdapter`,
`KayitCardAdapter`, `OdemelerCardAdapter`, `ViewPagerAdapter`.

**ViewModel'ler (→ StateFlow'a):** `companyViewModel`, `surgunViewModel`, `tarihViewModel`
— hepsi şu an `MutableLiveData<ArrayList<String>>` tutuyor, basit.

**Data class'lar (korunur):** `BahceRapor`, `kayitRapor`, `paymentData`, `PieChartData`,
`SurumRapor`, `YılRapor`, ayrıca `TeaGardens` / `TeaHarverst` entity'leri.

---

## 2. Migration Öncesi Engeller

1. **Compose bağımlılığı yok** → BOM + Material3 + navigation-compose + activity-compose +
   lifecycle-viewmodel-compose + lifecycle-runtime-compose eklenecek.
2. **`jvmTarget = "1.8"` / `JavaVersion.VERSION_1_8`** → Compose için 11'e yükseltilecek.
3. **Compose Compiler** → Kotlin 2.1.0 kullanıldığı için `org.jetbrains.kotlin.plugin.compose`
   plugin'i eklenecek (ayrı compiler sürümü gerekmiyor).
4. **Hilt yok** → `com.google.dagger.hilt.android` plugin + KSP + `@HiltAndroidApp` Application.
5. **MPAndroidChart** → ilk etapta `AndroidView` ile sarılır; raporlar fazında Compose'a taşınır.
6. **Raw SQLite** → iş mantığı `TeaRepository` arkasına alınır; ViewModel'ler repository'i çağırır.
   Room migrasyonu **opsiyonel** ve en sona bırakılır (davranış riskini azaltmak için).
7. **ViewBinding** kalır (hibrit dönemde eski Fragment'lar çalışmaya devam etsin diye); en sonda kaldırılır.

---

## 3. Hedef Paket Yapısı

```
com.erdem.designexample/
├── DesignApp.kt                 (@HiltAndroidApp)
├── MainActivity.kt              (setContent + NavHost)
├── ui/
│   ├── theme/                   Color.kt, Type.kt, Theme.kt (Light/Dark)
│   ├── components/              AppButton, AppTextField, RecordCard, FilterSheet, AppTopBar...
│   ├── navigation/              AppNavHost.kt, Destinations.kt (type-safe routes)
│   ├── auth/                    LoginScreen, SignUpScreen, AuthViewModel
│   ├── home/                    HomeScreen (TabRow + Pager)
│   ├── input/                   DevletScreen, OzelScreen, InputViewModel
│   ├── records/                 RecordsScreen, RecordsViewModel, RecordFilterSheet
│   ├── payments/                PaymentsScreen, PaymentsViewModel, PaymentFilterSheet
│   └── reports/                 ReportsScreen, ChartSection, ReportsViewModel
├── data/
│   ├── local/                   DatabaseHelper, DatabaseOperations (mevcut, korunur)
│   ├── remote/                  FirebaseSyncHelper (mevcut, korunur)
│   ├── model/                   data class'lar (mevcut)
│   └── repository/              TeaRepository, AuthRepository
└── di/                          AppModule (Hilt)
```

---

## 4. Faz Planı

### Faz 0 — Altyapı (ekran değişmeden, hibrit mod açılır)
- [ ] `libs.versions.toml`: Compose BOM, Material3, navigation-compose, activity-compose,
      lifecycle-viewmodel-compose, lifecycle-runtime-compose, hilt, hilt-navigation-compose, KSP.
- [ ] `app/build.gradle.kts`: `buildFeatures { compose = true }`, compose plugin, hilt plugin,
      `jvmTarget = "11"`, Java 11.
- [ ] `ui/theme/` — Color/Type/Theme (Çaykur yeşili temelli premium palet, Light + Dark).
- [ ] `DesignApp : Application` + `@HiltAndroidApp`, manifest'e `android:name`.
- [ ] `di/AppModule` — Repository / Firebase / DatabaseHelper provide.
- **Çıktı:** Proje derlenir, eski XML akışı aynen çalışır, Compose kullanıma hazır.
- **Risk:** Düşük. **Test:** Uygulama eskisi gibi açılıyor mu?

### Faz 1 — Auth akışı (en izole)
- [ ] `AuthRepository` (FirebaseAuth sarmalı), `AuthViewModel` (StateFlow: idle/loading/success/error).
- [ ] `LoginScreen` + `SignUpScreen` (`@Preview`'li), reusable `AppTextField` / `AppButton`.
- [ ] Geçici köprü: MainActivity başlangıçta auth durumuna göre Compose ekranını gösterir.
- **Çıktı:** Login/SignUp tamamen Compose. **Risk:** Düşük (dış bağımlılık az).

### Faz 2 — İskelet & Navigation
- [ ] `MainActivity` → `setContent { AppTheme { AppNavHost() } }`.
- [ ] `AppNavHost` + type-safe `Destinations` (auth, home, records, payments, input, reports).
- [ ] Compose `Scaffold` + `NavigationBar` (eski `bottom_bar_menu.xml` yerine).
- [ ] `HomeScreen`: `TabRow` + `HorizontalPager` (ÇAYKUR / Özel) — ViewPagerAdapter yerine.
- **Çıktı:** Tüm navigasyon Compose. Eski nav graph + Fragment'lar devreden çıkar.
- **Risk:** Orta (geri tuşu, deep link, sekme durumu dikkat).

### Faz 3 — Hasat girişi
- [ ] `TeaRepository` — `DatabaseOperations.add(...)` ve ilgili sorgular sarılır.
- [ ] `InputViewModel` (StateFlow form state + validasyon).
- [ ] `DevletScreen` (ÇAYKUR) + `OzelScreen` (Özel) — date picker, dropdown'lar Compose M3.
- **Çıktı:** Veri girişi Compose. **Risk:** Orta (validasyon + tarih/sezon mantığı korunmalı).

### Faz 4 — Kayıt & Ödeme listeleri
- [ ] `RecordsScreen` + `RecordsViewModel` — `LazyColumn` + `RecordCard` (KayitCardAdapter yerine).
- [ ] `PaymentsScreen` + `PaymentsViewModel` — `LazyColumn` + `PaymentCard`.
- [ ] Filtreler → `ModalBottomSheet` (`KayitFilterDialog` / `filterListDialog` yerine).
- **Çıktı:** Listeler + filtreler Compose. RecyclerView/Adapter'lar silinebilir.
- **Risk:** Orta (filtre mantığı + silme/sync etkileşimleri).

### Faz 5 — Raporlar & Grafik
- [ ] `ReportsScreen` + `ReportsViewModel` (BahceRapor / YılRapor / SurumRapor).
- [ ] Pie chart: önce `AndroidView { PieChart(...) }`, sonra Compose Canvas/Material chart.
- [ ] `ikinciFragment` / `birinciFragment` / `bahceRapor` karşılıkları.
- **Risk:** Orta-yüksek (grafik dönüşümü en zahmetli kısım).

### Faz 6 — Temizlik
- [ ] Tüm eski Fragment, Adapter, XML layout, nav graph, menu sil.
- [ ] ViewBinding kapat (`buildFeatures.viewBinding = false`).
- [ ] (Opsiyonel) SQLite → Room migrasyonu.
- [ ] Kullanılmayan bağımlılıkları (recyclerview, constraintlayout, navigation-fragment) çıkar.

---

## 5. Tasarım Sistemi (Material 3)

- **Renk:** Çay yaprağı yeşili ana renk; nötr yüzeyler, bol whitespace, premium his. Dynamic color
  (Android 12+) opsiyonel; marka tutarlılığı için sabit palet önerilir. Light + Dark zorunlu.
- **Tipografi:** M3 type scale, net hiyerarşi (display/headline/title/body/label).
- **Bileşenler:** `Card` (yumuşak elevation, yuvarlak köşe), `FilledButton`, `OutlinedTextField`,
  `ModalBottomSheet`, `NavigationBar`, `TopAppBar`.
- **Animasyon:** `AnimatedVisibility`, `animate*AsState` — zarif ve anlamlı.
- **Erişilebilirlik:** contentDescription, yeterli kontrast, min 48dp dokunma alanı.

---

## 6. Çapraz Kesişen İşler

- **LiveData → StateFlow:** 3 ViewModel basit; doğrudan dönüştürülür.
- **Firebase sync:** `FirebaseSyncHelper` korunur; uygun yaşam döngüsü noktasında (auth sonrası,
  liste açılışında) coroutine ile çağrılır.
- **`attachBaseContext` fontScale override:** Activity'de korunur.
- **Edge-to-edge:** `enableEdgeToEdge()` + Compose insets ile yeniden kurulur.

---

## 7. Riskler & Önlemler

| Risk | Önlem |
|------|-------|
| Tarih/sezon hesap mantığı bozulur | Repository'e taşırken davranışı birebir koru, eski ekranla karşılaştır |
| Firestore senkron sırası | Mevcut akışı değiştirme, sadece çağırma noktasını taşı |
| Grafik dönüşümü | Önce `AndroidView` köprüsü, görsel doğrulandıktan sonra Compose'a geç |
| Tek seferde çok değişiklik | Faz başına ayrı commit; eski sürüm silinmeden yenisi test edilir |
| Geri tuşu / sekme durumu | Faz 2'de navigation davranışını manuel test et |

---

## 8. Önerilen İlerleme

**Faz 0 → 1 → 2** sırası en güvenli temeldir. Her faz sonunda derleme + manuel test +
ayrı commit. Onay sonrası **Faz 0**'dan başlanması önerilir.
