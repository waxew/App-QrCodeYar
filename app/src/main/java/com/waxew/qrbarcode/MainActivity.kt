/*
 * نقطه ورود App-QrCodeYar.
 * سرویس‌های اصلی و تنظیمات نسخه 1.9 فقط یک بار ساخته می‌شوند و به ریشه Compose تزریق می‌شوند.
 */
package com.waxew.qrbarcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.ui.QrCodeYarV19Root
import com.waxew.qrbarcode.ui.theme.QrStudioTheme
import com.waxew.qrbarcode.v19.V19AppLock
import com.waxew.qrbarcode.v19.V19SettingsRepository

class MainActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager
    private lateinit var preferences: PreferencesRepository
    private lateinit var v19Settings: V19SettingsRepository
    private lateinit var appLock: V19AppLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this)
        preferences = PreferencesRepository(this)
        v19Settings = V19SettingsRepository(this)
        appLock = V19AppLock(this)

        setContent {
            QrStudioTheme {
                QrCodeYarV19Root(
                    activity = this,
                    billingManager = billingManager,
                    preferences = preferences,
                    settings = v19Settings,
                    appLock = appLock
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::billingManager.isInitialized) billingManager.refresh()
    }

    override fun onDestroy() {
        if (::billingManager.isInitialized) billingManager.close()
        if (::preferences.isInitialized) preferences.close()
        super.onDestroy()
    }
}
