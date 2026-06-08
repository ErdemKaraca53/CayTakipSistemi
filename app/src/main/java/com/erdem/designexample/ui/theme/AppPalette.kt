package com.erdem.designexample.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Koyu navy + teal/amber premium palet. Ana ekran ve onunla aynı dili paylaşan ekranlarda
 * (kayıt ekleme vb.) tema renklerinden bağımsız, sabit olarak kullanılır.
 */
object AppPalette {
    val Bg = Color(0xFF0E1621)          // Sayfa zemini (en koyu)
    val Card = Color(0xFF18222E)        // Kart zemini
    val Field = Color(0xFF0E1621)       // Form alanı dolgusu (kart üzerinde inset görünüm)
    val Border = Color(0xFF273341)      // İnce kenarlık
    val Teal = Color(0xFF17B9A6)        // Birincil vurgu
    val Amber = Color(0xFFE7A33C)       // İkincil vurgu / dikkat
    val TextPrimary = Color(0xFFEDF1F5) // Ana metin
    val TextSecondary = Color(0xFF8B98A7) // İkincil/etiket metni
}

/**
 * Koyu zeminli ekranlarda status bar ikonlarını daima açık renge zorlar (açık/koyu mod fark etmez);
 * ekrandan çıkınca önceki duruma döner.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            val previous = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose { controller.isAppearanceLightStatusBars = previous }
        }
    }
}
