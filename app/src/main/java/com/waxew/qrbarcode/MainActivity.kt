package com.waxew.qrbarcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.ui.QrBarcodeApp
import com.waxew.qrbarcode.ui.theme.QrStudioTheme

class MainActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager

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

    override fun onResume() {
        super.onResume()
        if (::billingManager.isInitialized) billingManager.refresh()
    }

    override fun onDestroy() {
        if (::billingManager.isInitialized) billingManager.close()
        super.onDestroy()
    }
}
