/*
 * Compatibility bridge for the previous JourneyApps API used by QrBarcodeApp.kt.
 *
 * Version 1.1 replaces the external CaptureActivity with our own CameraX + ML Kit scanner,
 * but keeping the small ScanContract/ScanOptions surface means the existing UI does not need
 * a risky large rewrite. No code from JourneyApps is copied here; this is a tiny app-owned API.
 */
package com.journeyapps.barcodescanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.waxew.qrbarcode.scanner.ModernScannerActivity

class ScanOptions {
    internal var prompt: String = "کد را داخل کادر بگیر"
    internal var beepEnabled: Boolean = true
    internal var torchEnabled: Boolean = false
    internal var orientationLocked: Boolean = false

    fun setPrompt(value: String) = apply { prompt = value }
    fun setBeepEnabled(value: Boolean) = apply { beepEnabled = value }
    fun setTorchEnabled(value: Boolean) = apply { torchEnabled = value }
    fun setOrientationLocked(value: Boolean) = apply { orientationLocked = value }
}

data class ScanIntentResult(
    val contents: String?,
    val formatName: String?
)

class ScanContract : ActivityResultContract<ScanOptions, ScanIntentResult>() {
    companion object {
        const val EXTRA_PROMPT = "qrcodeyar.scan.PROMPT"
        const val EXTRA_BEEP = "qrcodeyar.scan.BEEP"
        const val EXTRA_TORCH = "qrcodeyar.scan.TORCH"
        const val EXTRA_CONTENTS = "qrcodeyar.scan.CONTENTS"
        const val EXTRA_FORMAT = "qrcodeyar.scan.FORMAT"
    }

    override fun createIntent(context: Context, input: ScanOptions): Intent =
        Intent(context, ModernScannerActivity::class.java).apply {
            putExtra(EXTRA_PROMPT, input.prompt)
            putExtra(EXTRA_BEEP, input.beepEnabled)
            putExtra(EXTRA_TORCH, input.torchEnabled)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        if (resultCode != Activity.RESULT_OK || intent == null) return ScanIntentResult(null, null)
        return ScanIntentResult(
            contents = intent.getStringExtra(EXTRA_CONTENTS),
            formatName = intent.getStringExtra(EXTRA_FORMAT)
        )
    }
}
