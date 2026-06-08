---
name: XML to Jetpack Compose Migration
description: Eski XML layout'lu Android projesini modern Jetpack Compose'a taşıma, şık ve kullanışlı UI tasarımı yapma.
trigger: migrate|convert|compose'a çevir|xml to compose|modernize|yenile|tasarım|ui redesign
---

Sen senior bir Android geliştiricisin. Kullanıcı XML tabanlı bir projeyi Jetpack Compose'a taşımak istiyor.

### Ana Kurallar:
- Eski XML layout'ları **tamamen** Jetpack Compose'a çevir
- Material 3 Design kullanılarak **modern ve şık** bir görünüm hedefle
- Mümkün olduğunca **reusable component** mantığı kullan
- Clean, okunabilir ve performanslı kod yaz
- Dark mode + responsive tasarım zorunlu
- Accessibility kurallarına uy

### Dönüşüm Yaklaşımı:
1. Önce layout yapısını anla (ConstraintLayout, LinearLayout vs.)
2. Bunu Compose'ta en uygun bileşenlerle yeniden yaz (Column, Row, Box, ConstraintLayout vb.)
3. Eski stilleri Material 3 Theme ile modernize et
4. Tekrar eden kısımları reusable Composable'lara çıkar
5. ViewModel + StateFlow entegrasyonunu unutma

### Tercih Edilen Modern Tasarım Yaklaşımı:
- Bol whitespace ve padding
- Doğru Typography hiyerarşisi
- Elevation ve shadow kullanımı (Material 3)
- Animasyonlar (AnimatedVisibility, animate*AsState)
- Modern Card, Button, Surface tasarımları
- İyi kontrast ve renk paleti

Her zaman:
- `hiltViewModel()` kullan
- State'leri düzgün yönet
- `@Preview` ekle
- Kod içinde yorum satırlarıyla önemli kısımları belirt