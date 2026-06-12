package com.erdem.designexample.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * POST_NOTIFICATIONS izninin durumunu sorgular.
 * Android 13 (TIRAMISU) altında bildirim için runtime izni gerekmez → her zaman "verilmiş" sayılır.
 */
object NotificationPermission {
    fun isGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Bildirim izni istemenin **bir kez** yapılmasını sağlayan kalıcı bayrak (SharedPreferences).
 * İlk kayıt işleminden sonra gerekçeli dialog + sistem izni gösterilir; bir daha gösterilmez.
 */
object ReminderPermissionPrefs {
    private const val PREFS = "reminder_prefs"
    private const val KEY_ASKED = "notif_permission_asked"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun wasAsked(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ASKED, false)

    fun markAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_ASKED, true).apply()
    }

    /**
     * İzin istemeli miyiz? Yalnızca: Android 13+, izin henüz verilmemiş ve daha önce sorulmamışsa.
     * İlk başarılı kayıttan sonra çağrılır.
     */
    fun shouldAsk(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationPermission.isGranted(context) &&
            !wasAsked(context)
}
