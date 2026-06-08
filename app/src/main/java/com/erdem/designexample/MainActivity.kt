package com.erdem.designexample

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.erdem.designexample.ui.AppRoot
import com.erdem.designexample.ui.theme.DesignExampleTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Tek Activity. Tüm UI Jetpack Compose ile [AppRoot] içinde yönetilir.
 * Eski XML nav graph + Fragment iskeleti emekliye ayrıldı (Faz 3).
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DesignExampleTheme {
                AppRoot()
            }
        }
    }

    /** Eski davranış korunuyor: sistem font ölçeklemesini sabitler (layout güvenliği). */
    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
    }
}
