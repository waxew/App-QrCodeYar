/*
 * Compatibility bridge for the CameraX + ML Kit scanner.
 *
 * تنظیمات نسخه 1.9 از V19SettingsRepository خوانده می‌شوند و به Activity اسکنر منتقل می‌شوند؛
 * بنابراین صدای اسکن، لرزش، حالت متوالی و جلوگیری از تکرار فقط گزینه نمایشی نیستند.
 */
package com.journeyapps.barcodescanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.waxew.qrbarcode.scanner.V19ScannerActivity
import com.waxew.qrbarcode.v19.V19SettingsRepository

class ScanOptions {
    internal var prompt: String = "کد را داخل کادر بگیر"
    internal var beepEnabled: Boolean? = null
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
        const val EXTRA_VIBRATE = "qrcodeyar.scan.VIBRATE"
        const val EXTRA_CONTINUOUS = "qrcodeyar.scan.CONTINUOUS"
        const val EXTRA_PREVENT_DUPLICATES = "qrcodeyar.scan.PREVENT_DUPLICATES"
        const val EXTRA_TORCH = "qrcodeyar.scan.TORCH"
        const val EXTRA_CONTENTS = "qrcodeyar.scan.CONTENTS"
        const val EXTRA_FORMAT = "qrcodeyar.scan.FORMAT"
    }

    override fun createIntent(context: Context, input: ScanOptions): Intent {
        val settings = V19SettingsRepository(context)
        return Intent(context, V19ScannerActivity::class.java).apply {
            putExtra(EXTRA_PROMPT, input.prompt)
            putExtra(EXTRA_BEEP, input.beepEnabled ?: settings.scannerBeep)
            putExtra(EXTRA_VIBRATE, settings.scannerVibrate)
            putExtra(EXTRA_CONTINUOUS, settings.continuousScan)
            putExtra(EXTRA_PREVENT_DUPLICATES, settings.preventDuplicates)
            putExtra(EXTRA_TORCH, input.torchEnabled)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        if (resultCode != Activity.RESULT_OK || intent == null) return ScanIntentResult(null, null)
        return ScanIntentResult(
            contents = intent.getStringExtra(EXTRA_CONTENTS),
            formatName = intent.getStringExtra(EXTRA_FORMAT)
        )
    }
}
