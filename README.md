# 🍃 Çay Defteri (ÇAYKUR)

> Çay üreticileri için **hasat & ödeme takip** uygulaması. ÇAYKUR ve özel alıcı satışlarını kg/fiyat/vade bazında tek yerden yönetin.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Min%20SDK-26-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=for-the-badge" />
</p>

---

## 📖 İçindekiler

- [Genel Bakış](#-genel-bakış)
- [Özellikler](#-özellikler)
- [Kullanılan Teknolojiler](#-kullanılan-teknolojiler)
- [Mimari](#-mimari)
- [Proje Yapısı](#-proje-yapısı)
- [Veri Modeli](#-veri-modeli)
- [Navigasyon Haritası](#-navigasyon-haritası)
- [Kurulum](#-kurulum)
- [Build & Komutlar](#-build--komutlar)
- [Yol Haritası](#-yol-haritası)

---

## 🌱 Genel Bakış

**Çay Defteri**, küçük ölçekli çay üreticilerinin hasat kayıtlarını, satış bilgilerini ve ödeme vadelerini dijital olarak takip etmesini sağlayan bir Android uygulamasıdır. İki satış kanalını destekler:

| Kanal | Açıklama |
|---|---|
| 🟢 **ÇAYKUR** | Sabit `35 TL/Kg` fiyat, vade tarihi otomatik hesaplanır (satış ayının bir sonraki ayının son günü) |
| 🔵 **Özel Alıcı** | Serbest fiyat, serbest satış yeri (firma adı) ve serbest vade tarihi |

Uygulama tamamen **Jetpack Compose + Material 3** ile yeniden tasarlanmış, koyu **navy/teal** temelli premium bir görsel dil kullanır ve **Dark/Light mode** destekler.

---

## ✨ Özellikler

- 📊 **Dashboard** — büyük dokunmatik kartlarla (Yeni Kayıt, Kayıtlar, Ödemeler, Raporlar) hızlı erişim
- 📝 **Hasat Girişi** — ÇAYKUR / Özel sekmeleri arasında kaydırmalı (`HorizontalPager`) form
  - Bahçe adı ve firma adı için **autocomplete** önerileri
  - Wheel-picker tarzı tarih seçici
  - Boş/zorunlu alanlar **kırmızı kenarlıkla** vurgulanır + merkezi uyarı dialog'u
  - Türkçe ondalık (virgül) giriş desteği
- 📋 **Kayıtlar** — geçmiş hasatların listesi; bahçe, sürüm, kg, fiyat, toplam ve **satış yeri** bilgisiyle
  - Düzenleme ve silme
- 💰 **Ödemeler** — vade tarihine göre ödeme durumu takibi
- 🔔 **Bildirimler** — `WorkManager` ile vadesi yaklaşan ödemeler için arka plan hatırlatmaları
- 📈 **Raporlar** — `DonutChart` ile özet grafikler
- 🔐 **Kimlik Doğrulama** — Firebase Authentication (e-posta/şifre + Google Sign-In)
- ☁️ **Bulut Senkronizasyonu** — Firestore üzerinde `users/{uid}/harvests` koleksiyonu, offline-first (Firestore yerel cache + otomatik senkron)
- 🌗 **Dark / Light Mode** — özel `AppPalette` renk paleti ile her iki temada tutarlı görünüm

---

## 🛠 Kullanılan Teknolojiler

### Dil & Derleyici
| Teknoloji | Versiyon |
|---|---|
| **Kotlin** | 2.1.0 |
| **Android Gradle Plugin** | 8.11.0 |
| **Java (jvmTarget)** | 11 |
| **Compile/Target/Min SDK** | 36 / 35 / 26 |

### UI Katmanı
| Teknoloji | Açıklama |
|---|---|
| **Jetpack Compose** (BOM 2025.01.00) | Tüm UI deklaratif olarak Compose ile yazılıyor |
| **Material 3** | Tasarım sistemi — renkler, tipografi, bileşenler |
| **Compose Material Icons Extended** | Outlined ikon seti (`Icons.Outlined.*`) |
| **Navigation Compose** (2.8.5) | Tek-Activity, string tabanlı rota navigasyonu + geçiş animasyonları |
| **Compose Foundation Pager** | `HorizontalPager` ile sekme/sayfa kaydırma (kayıt formu) |

### Mimari & DI
| Teknoloji | Açıklama |
|---|---|
| **MVVM** | Her ekran `XxxScreen` (Composable) + `XxxViewModel` |
| **StateFlow / Kotlin Coroutines** | Reaktif UI state yönetimi |
| **Hilt** (2.52) | Dependency Injection — `@HiltViewModel`, `@AndroidEntryPoint` |
| **KSP** | Annotation processing (Hilt + Room) — `kapt` kullanılmıyor |

### Veri & Depolama
| Teknoloji | Açıklama |
|---|---|
| **Room** (2.6.1) | Yerel veritabanı (`tea_diary.db`) — `Harvest`, `Garden`, `Seller` entity'leri |
| **Firebase Firestore** | Bulut yedekleme — `users/{uid}/harvests/{id}` |
| **Firebase Authentication** | E-posta/şifre + Google Sign-In (Credential Manager + Google ID) |
| **Firebase BoM** | 33.11.0 |

### Arka Plan İşlemleri
| Teknoloji | Açıklama |
|---|---|
| **WorkManager** (2.9.1) | Ödeme vadesi hatırlatma bildirimleri |

### Test
| Teknoloji | Açıklama |
|---|---|
| **JUnit 4** | Birim testler |
| **Espresso** | Instrumented testler |

---

## 🏗 Mimari

```
┌──────────────────────────────────────────────────────────────┐
│                          MainActivity                          │
│                  @AndroidEntryPoint · setContent              │
└───────────────────────────┬───────────────────────────────────┘
                             │
                  DesignExampleTheme { AppRoot() }
                             │
              ┌──────────────┴───────────────┐
              │     NavController kurulur     │
              │ Başlangıç hedefi: FirebaseAuth │
              │  oturumu varsa → DASHBOARD     │
              │  yoksa        → SIGNUP         │
              └──────────────┬───────────────┘
                             │
                       AppNavHost
        (tüm rotalar + slide/fade geçiş animasyonları)
                             │
   ┌──────────┬──────────┬──────────┬──────────┬──────────┐
   │  auth/   │dashboard/│  home/   │ records/ │payments/ │ reports/
   │ (Login,  │(MainSection│(RecordEntry│ (liste, │ (vade   │ (Donut
   │ Signup)  │  kartları) │  formu)   │ düzenle)│ takibi) │  grafik)
   └──────────┴──────────┴──────────┴──────────┴──────────┘
```

### Katman Sorumlulukları

```
ui/<özellik>/        → XxxScreen (Composable) + XxxViewModel (@HiltViewModel, StateFlow)
ui/components/       → Yeniden kullanılabilir composable'lar
                        (AppButton, AppTextField, RecordCard, TonalFields/WheelPicker, DonutChart)
ui/theme/            → DesignExampleTheme + AppPalette (ÇAYKUR yeşili tabanlı Light+Dark palet)
data/repository/     → HarvestRepository — ViewModel'lerin TEK veri erişim noktası
data/db/             → Room (AppDatabase = tea_diary.db, Harvest/Garden/Seller entity + DAO)
di/                  → DatabaseModule (Room+DAO), AppModule (legacy DatabaseHelper,
                        FirebaseSyncHelper, FirebaseAuth)
```

> 💡 **Veri akışı:** `HarvestEntity` Room'da saklanır (kaynak doğruluk), Firestore'a
> `users/{uid}/harvests/{id}` altında yedeklenir. Giriş başarılı olduğunda
> `FirebaseSyncHelper.syncFirestoreToSQLite()` çağrılarak bulut → yerel senkron yapılır.
> Firestore'un varsayılan offline-persistence özelliği sayesinde, internet yokken
> yapılan yazımlar yerel kuyruğa alınır ve bağlantı geri geldiğinde otomatik senkronlanır.

---

## 📁 Proje Yapısı

```
app/src/main/java/com/erdem/designexample/
├── ui/
│   ├── auth/            → Login & Signup ekranları
│   ├── dashboard/        → Ana ekran (büyük navigasyon kartları)
│   ├── home/             → RecordEntryScreen (Yeni Kayıt / Düzenleme)
│   ├── input/             → DevletInputScreen (ÇAYKUR), OzelInputScreen (Özel), RecordEntryViewModel
│   ├── records/          → RecordsScreen (kayıt listesi)
│   ├── payments/         → PaymentsScreen (vade/tahsilat takibi)
│   ├── reports/           → ReportsScreen (özet & grafikler)
│   ├── components/        → AppButton, AppTextField, TonalFields (TextField/Autocomplete/
│   │                         Dropdown/DateField), RecordCard, DonutChart, SegmentedTabs
│   ├── theme/             → DesignExampleTheme, AppPalette, Color.kt
│   └── navigation/        → Destinations.kt (Routes, MainSection), AppNavHost
├── data/
│   ├── db/                → AppDatabase (Room), entity/ (Harvest, Garden, Seller), dao/
│   └── repository/        → HarvestRepository
├── database/              → Legacy: FirebaseSyncHelper, DatabaseHelper, DatabaseOperations
├── notifications/         → PaymentReminderWorker, PaymentReminderScheduler,
│                             ReminderPermissionPrefs (WorkManager bildirimleri)
├── di/                    → DatabaseModule, AppModule (Hilt)
└── MainActivity.kt        → @AndroidEntryPoint, tek giriş noktası
```

> ⚠️ **Not:** `design/` (Fragment'lar), `adapter/`, `viewModels/` (LiveData), `dataClass/`,
> `res/layout/*.xml` (19 layout) gibi **legacy XML-tabanlı kod hâlâ ağaçta** fakat
> aktif Compose akışına bağlı değil (temizlik bekliyor). Tek istisna:
> `database/FirebaseSyncHelper` ve `database/DatabaseHelper`/`DatabaseOperations`,
> `AppModule` üzerinden hâlâ kullanımda.

---

## 🗄 Veri Modeli

**`HarvestEntity`** (Room — `tea_diary.db`)

| Alan | Tip | Açıklama |
|---|---|---|
| `id` | `Long` | Room autoGenerate primary key |
| `gardenName` | `String` | Bahçe adı (kanonik, autocomplete havuzundan çözülür) |
| `harvestDateEpoch` | `Long` | Hasat tarihi — **epoch-day** (`LocalDate.toEpochDay()`) |
| `season` | `Int` | Sürüm — `1..4` |
| `weightKg` | `Float` | Miktar (kg) |
| `pricePerKg` | `Float` | Fiyat (TL/kg) — ÇAYKUR'da sabit `35` |
| `source` | `String` | `"CAYKUR"` \| `"OZEL"` |
| `companyName` | `String` | Satış yeri / firma — ÇAYKUR'da sabit `"ÇAYKUR"` |
| `dueDateEpoch` | `Long` | Vade tarihi — epoch-day |
| `createdAt` | `Long` | Oluşturulma zaman damgası (epoch millis) |

> 📅 Tüm tarihler `LocalDate.toEpochDay()` / `LocalDate.ofEpochDay()` ile epoch-day `Long` olarak saklanır.
>
> 💱 ÇAYKUR vade tarihi: **satış ayının bir sonraki ayının son günü** (örn. 3 Temmuz → 31 Ağustos).

---

## 🧭 Navigasyon Haritası

```
SIGNUP ──┐
         ├──► LOGIN ──► DASHBOARD ──┬──► RECORD_ENTRY (?editId={id})
                                     ├──► RECORDS
                                     ├──► PAYMENTS
                                     ├──► REPORTS
                                     └──► SETTINGS
```

| Rota | Sabit | Açıklama |
|---|---|---|
| `signup` | `Routes.SIGNUP` | Kayıt ol |
| `login` | `Routes.LOGIN` | Giriş yap |
| `dashboard` | `Routes.DASHBOARD` | Ana ekran — `MainSection` kartları |
| `record_entry?editId={id}` | `Routes.recordEntryEdit(id)` | `editId=-1` → yeni kayıt, `>0` → düzenleme |
| `records` | `Routes.RECORDS` | Kayıt listesi |
| `payments` | `Routes.PAYMENTS` | Ödeme/vade takibi |
| `reports` | `Routes.REPORTS` | Raporlar & grafikler |
| `settings` | `Routes.SETTINGS` | Ayarlar |

Başlangıç hedefi `AppRoot` tarafından **FirebaseAuth oturum durumuna** göre seçilir: oturum açıksa `DASHBOARD`, değilse `SIGNUP`.

---

## ⚙️ Kurulum

1. Bu repoyu klonlayın.
2. Firebase projenizden `google-services.json` dosyasını indirip `app/` klasörüne yerleştirin.
3. Firestore güvenlik kurallarını `users/{userId}/harvests/{document}` yolunu kapsayacak şekilde ayarlayın:

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /harvests/{document} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

4. Android Studio ile projeyi açın ve senkronize edin.

---

## 🔨 Build & Komutlar

> Wrapper Windows'ta `gradlew.bat`'tır.

```bash
gradlew.bat assembleDebug          # Debug APK derle
gradlew.bat installDebug           # Bağlı cihaza yükle
gradlew.bat test                   # JVM unit testleri (testDebugUnitTest)
gradlew.bat connectedAndroidTest   # Cihaz/emulatör instrumented testleri
gradlew.bat lint                   # Android Lint

# Tek test sınıfı:
gradlew.bat test --tests "com.erdem.designexample.SomeTest"
```

---

## 🗺 Yol Haritası

- [ ] Release `signingConfig` yapılandırması
- [ ] Şifre sıfırlama akışı (Login ekranı)
- [ ] Manuel "ödendi" işaretleme (şu an tamamen tarihe bağlı)
- [ ] Yinelenen kayıt uyarısı
- [ ] Veri dışa aktarma (export)
- [ ] Kayıtlarda arama / sıralama
- [ ] Legacy XML kod temizliği (`design/`, `adapter/`, `viewModels/`, `dataClass/`, eski layout'lar, `FirebaseSyncHelper`/`DatabaseHelper`)

---

<p align="center">🍃 <b>ÇAYKUR Çay Defteri</b> — Modern Android Compose ile geliştirildi 🍃</p>
