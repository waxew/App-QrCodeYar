/*
 * نقطه ورود اپلیکیشن.
 * BillingManager و PreferencesRepository فقط یک‌بار ساخته می‌شوند و به ریشه Compose تزریق می‌شوند.
 */
package com.waxew.qrbarcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.ui.QrBarcodeApp
import com.waxew.qrbarcode.ui.theme.QrStudioTheme

class MainActivity : ComponentActivity() {
    // کلاینت پرداخت تا پایان عمر Activity نگه داشته می‌شود.
    private lateinit var billingManager: BillingManager

    // Android این متد را هنگام ایجاد Activity فراخوانی می‌کند.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this)
        val preferences = PreferencesRepository(this)
        setContent {
            QrStudioTheme {
                QrBarcodeApp(
                    activity = this,
                    billingManager = billingManager,
                    preferences = preferences
                )
            }
        }
    }

    // بعد از برگشت از صفحه پرداخت، وضعیت خرید دوباره استعلام می‌شود.
    override fun onResume() {
        super.onResume()
        if (::billingManager.isInitialized) billingManager.refresh()
    }

    // اتصال Billing در پایان Activity آزاد می‌شود تا resource leak ایجاد نشود.
    override fun onDestroy() {
        if (::billingManager.isInitialized) billingManager.close()
        super.onDestroy()
    }
}
