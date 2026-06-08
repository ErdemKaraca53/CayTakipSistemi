package com.erdem.designexample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Uygulama giriş noktası. Hilt bağımlılık enjeksiyonunu başlatır.
 * Manifest'te android:name=".DesignApp" olarak tanımlıdır.
 */
@HiltAndroidApp
class DesignApp : Application()
