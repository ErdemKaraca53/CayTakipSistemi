---
name: Full Android Compose Migration Project
version: 1.2
---

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Komutlar

> **Önemli:** Bu projede tüm Gradle build / compile / run işlemlerini **kullanıcı kendisi yapar**.
> Claude bu komutları **çalıştırmaz**; aşağıdakiler yalnızca referanstır. Wrapper Windows'ta `gradlew.bat`.

```bash
gradlew.bat assembleDebug          # Debug APK derle
gradlew.bat installDebug           # Bağlı cihaza yükle
gradlew.bat test                   # JVM unit testleri (testDebugUnitTest)
gradlew.bat connectedAndroidTest   # Cihaz/emulatör instrumented testleri
gradlew.bat lint                   # Android Lint
# Tek test sınıfı:
gradlew.bat test --tests "com.erdem.designexample.SomeTest"
```

- **Toolchain:** Kotlin 2.1.x, Java 11 (`jvmTarget = "11"`), `compileSdk 36`, `minSdk 26`, `targetSdk 35`.
- **Bağımlılıklar versiyon kataloğunda:** `gradle/libs.versions.toml` (`libs.*` alias'ları). Yeni kütüphane eklerken önce buraya tanımla.
- **Annotation processing KSP ile** (Hilt + Room). `kapt` kullanılmaz.
- **Firebase** `google-services` plugin'ine ve `app/google-services.json` dosyasına bağlıdır.

## Mimari (büyük resim)

Uygulama bir **çay hasadı & ödeme takip** uygulaması (ÇAYKUR / özel alıcı satışları, kg, fiyat, vade). XML→Compose migrasyonu **ileri seviyede**: `MainActivity` artık tamamen Compose'tur, eski Fragment/nav-graph iskeleti devre dışıdır.

**UI giriş zinciri:**
`MainActivity` (`@AndroidEntryPoint`, tek Activity) → `setContent { DesignExampleTheme { AppRoot() } }` → `AppRoot` (NavController kurar, başlangıç hedefini **FirebaseAuth oturumuna göre** seçer: oturum varsa `DASHBOARD`, yoksa `SIGNUP`) → `AppNavHost` (tüm rotalar + yatay kayma/fade geçiş animasyonları).

**Navigasyon:** Rotalar string sabittir — `ui/navigation/Destinations.kt` içindeki `Routes` objesi. Plan "type-safe navigation" der ama **mevcut hâli string-tabanlıdır** (`record_entry?editId={id}` gibi opsiyonel arg'lı). Alt çubuk **yoktur**; ana ekran `DashboardScreen` büyük kartlardan (`MainSection` enum) bölümlere gider. Düzenleme akışı: `Routes.recordEntryEdit(id)` → aynı `RecordEntryScreen`, `editId=-1` yeni kayıt demektir.

**Katmanlar (MVVM + Hilt):**
- `ui/<özellik>/` — her ekran `XxxScreen` composable + `XxxViewModel` (`@HiltViewModel`, `StateFlow` ile UI state). Ekranlar: `auth`, `dashboard`, `home` (RecordEntry), `records`, `payments`, `reports`.
- `ui/components/` — yeniden kullanılabilir composable'lar (`AppButton`, `AppTextField`, `RecordCard`, `TonalFields` içindeki `WheelPicker` tarih seçici, `DonutChart`, vb.).
- `ui/theme/` — `DesignExampleTheme` (Theme.kt), `AppPalette` (özel sabit renk paleti; ÇAYKUR yeşili temelli, Light+Dark).
- `data/repository/HarvestRepository` — ViewModel'lerin **tek veri erişim noktası**; DAO'lara doğrudan erişilmez. Filtre/öneri (bahçe & firma autocomplete) mantığı burada.
- `data/db/` — **Room** (`AppDatabase` = `tea_diary.db`, entity'ler `Harvest/Garden/Seller`, ilgili DAO'lar). Şu an `fallbackToDestructiveMigration` aktif (prodda gerçek Migration gerekli).
- `di/` — iki Hilt modülü: `DatabaseModule` (Room veritabanı + DAO'lar) ve `AppModule` (legacy `DatabaseHelper`/`DatabaseOperations`, `FirebaseSyncHelper`, `FirebaseAuth`).

**Veri modeli notu:** Tüm tarihler **epoch-day `Long`** olarak saklanır (`LocalDate.toEpochDay()` / `ofEpochDay()`). `HarvestEntity.source` = `"CAYKUR"` | `"OZEL"`; `season` = 1–4.

**Firebase senkron:** Login başarılı olunca `AppNavHost`, `FirebaseSyncHelper.syncFirestoreToSQLite()` çağırır (IO dispatcher). Yani veri hem **Room** hem **legacy SQLite + Firestore** yollarıyla yaşıyor — sync köprüsü `com.erdem.designexample.database` paketinde ve **hâlâ aktiftir**, silinmiş değildir.

## Migrasyon durumu — dikkat edilecekler

- `COMPOSE_MIGRATION_PLAN.md` faz planını içerir ama **kısmen güncel değildir** (ör. Room "opsiyonel/en son" deniyor; gerçekte zaten benimsenmiş). Kod gerçeği plan metninden önceliklidir.
- Ağaçta **eski (legacy) kod hâlâ duruyor** ve büyük kısmı bağlı değildir (Faz 6 temizliği bekliyor): `design/` (Fragment'lar), `adapter/`, `viewModels/`, `dataClass/`, `res/layout/*.xml` (19 layout), `viewBinding` hâlâ açık. Bunları "kullanılıyor" varsayma — gerçek giriş noktası Compose'tur. **İstisna:** `database/FirebaseSyncHelper` ve `database/DatabaseHelper`/`DatabaseOperations` `AppModule` üzerinden hâlâ kullanımda.
- Yeni ekran/bileşen eklerken legacy `viewModels/` (LiveData) değil, `ui/.../XxxViewModel` (StateFlow + Hilt) desenini izle.

---

## Proje Talimatları (kalıcı yönergeler)

Sen senior bir Android geliştiricisin. Bu projenin görevi: **Tüm eski XML tabanlı Android uygulamasını Jetpack Compose'a taşımak** ve **modern, şık, premium his veren** yeni bir tasarıma kavuşturmaktır.

### Proje Hedefi:
- Tamamen Jetpack Compose + Material 3 tabanlı yeni bir uygulama
- Eski XML layout'lar, Activity'ler ve Fragment'lar terk edilecek
- Yeni tasarım: Modern, temiz, bol whitespace, iyi tipografi, premium görünüm
- Dark/Light mode mükemmel destek
- Performans ve kod kalitesi yüksek olsun

### Genel Kurallar (Her Zaman Geçerli):

1. **Migration Stratejisi**
    - Tek seferde tüm projeyi çevirme. Ekran ekran, modül modül ilerle.
    - Önce en önemli ekranlardan başla.
    - Eski kodları silmeden önce yeni Compose versiyonunu test et.

2. **Tasarım Yaklaşımı**
    - Material 3 Design System kullan
    - Modern UI trendlerine uygun (yumuşak gölgeler, yuvarlak köşeler, bol boşluk)
    - Tutarlı renk paleti, typography ve component stili
    - Animasyonları zarif ve anlamlı kullan

3. **Mimari**
    - MVVM + StateFlow
    - Hilt DI
    - Clean Architecture prensiplerine uy
    - Type-safe Compose Navigation

4. **Kod Standartları**
    - Her ekran için ayrı `Screen` composable
    - Reusable component’leri `ui/components/` klasöründe topla
    - Her composable’a `@Preview` ekle
    - İyi isimlendirme ve yorum satırları

---

**Kullanıcı “tüm projeyi çevir” dediğinde:**
- Önce mevcut proje yapısını sor
- Migration için adım adım plan sun
- İlk olarak en kritik ekranları migrate etmeyi teklif et

Artık bu talimatlara göre hareket et.
