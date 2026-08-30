/*
 * App-QrCodeYar v2.0 - قفل Biometric/Device Credential.
 *
 * هیچ داده بیومتریک در برنامه ذخیره نمی‌شود. AndroidX Biometric فقط نتیجه احراز هویت
 * سیستم‌عامل را برمی‌گرداند و PIN محلی برنامه همچنان مسیر پشتیبان است.
 */
package com.waxew.qrbarcode.v20

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class V20Biometric(private val activity: FragmentActivity) {
    private val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun available(): Boolean = BiometricManager.from(activity).canAuthenticate(authenticators) ==
        BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        if (!available()) {
            onError("Biometric یا قفل امن دستگاه در دسترس نیست.")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_CANCELED) {
                    onError(errString.toString())
                }
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("باز کردن QR یار")
            .setSubtitle("با اثر انگشت، چهره یا قفل امن دستگاه وارد شوید")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }
}
