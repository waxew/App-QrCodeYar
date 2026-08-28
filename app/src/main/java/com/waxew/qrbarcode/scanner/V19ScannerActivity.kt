/*
 * App-QrCodeYar v1.9 - اسکنر متصل به تنظیمات واقعی کاربر
 *
 * این Activity چهار تنظیم نسخه 1.9 را واقعاً اجرا می‌کند:
 * 1) Beep هنگام تشخیص جدید
 * 2) Vibrate هنگام تشخیص جدید
 * 3) Continuous Scan برای نگه‌داشتن چند نتیجه در یک Session
 * 4) Prevent Duplicates برای جلوگیری از ثبت چندباره یک کد در Session
 */
package com.waxew.qrbarcode.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.journeyapps.barcodescanner.ScanContract
import com.waxew.qrbarcode.ui.theme.QrStudioTheme
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class V19ScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prompt = intent.getStringExtra(ScanContract.EXTRA_PROMPT) ?: "کد را داخل کادر بگیر"
        val beep = intent.getBooleanExtra(ScanContract.EXTRA_BEEP, true)
        val vibrate = intent.getBooleanExtra(ScanContract.EXTRA_VIBRATE, true)
        val continuous = intent.getBooleanExtra(ScanContract.EXTRA_CONTINUOUS, false)
        val preventDuplicates = intent.getBooleanExtra(ScanContract.EXTRA_PREVENT_DUPLICATES, true)
        val torch = intent.getBooleanExtra(ScanContract.EXTRA_TORCH, false)

        setContent {
            QrStudioTheme {
                ScannerContent(
                    prompt = prompt,
                    beepEnabled = beep,
                    vibrateEnabled = vibrate,
                    continuous = continuous,
                    preventDuplicates = preventDuplicates,
                    initialTorch = torch,
                    onResult = ::finishWithResult,
                    onCancel = { setResult(Activity.RESULT_CANCELED); finish() }
                )
            }
        }
    }

    private fun finishWithResult(code: DecodedImageCode) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(ScanContract.EXTRA_CONTENTS, code.text)
                putExtra(ScanContract.EXTRA_FORMAT, code.format)
            }
        )
        finish()
    }
}

@Composable
private fun ScannerContent(
    prompt: String,
    beepEnabled: Boolean,
    vibrateEnabled: Boolean,
    continuous: Boolean,
    preventDuplicates: Boolean,
    initialTorch: Boolean,
    onResult: (DecodedImageCode) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var permission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var detected by remember { mutableStateOf<List<DecodedImageCode>>(emptyList()) }
    var torch by remember { mutableStateOf(initialTorch) }
    var cameraState by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var autoReturned by remember { mutableStateOf(false) }

    val tone = remember(beepEnabled) { if (beepEnabled) ToneGenerator(AudioManager.STREAM_MUSIC, 65) else null }
    DisposableEffect(tone) { onDispose { tone?.release() } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permission = granted
        if (!granted) error = "دسترسی دوربین داده نشد."
    }

    LaunchedEffect(Unit) {
        if (!permission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun feedback() {
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
        if (vibrateEnabled) vibrateOnce(context)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("اسکنر حرفه‌ای 1.9", fontWeight = FontWeight.Black)
                    Text(
                        if (continuous) "حالت اسکن متوالی فعال است" else "بعد از اولین تشخیص معتبر برمی‌گردد",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedButton(onClick = onCancel) { Text("بستن") }
            }
            Text(prompt, style = MaterialTheme.typography.bodySmall)

            if (permission) {
                Box(Modifier.fillMaxWidth().height(390.dp)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                        },
                        update = { previewView ->
                            bindCamera(
                                activity = context as ComponentActivity,
                                previewView = previewView,
                                initialTorch = torch,
                                onCamera = { camera -> cameraState = camera },
                                onCodes = { incoming ->
                                    if (incoming.isEmpty()) return@bindCamera
                                    val oldKeys = detected.map { "${it.format}|${it.text}" }.toSet()
                                    val accepted = if (preventDuplicates) {
                                        incoming.filterNot { "${it.format}|${it.text}" in oldKeys }
                                    } else incoming
                                    if (accepted.isEmpty()) return@bindCamera
                                    feedback()

                                    if (!continuous && !autoReturned) {
                                        autoReturned = true
                                        onResult(accepted.first())
                                    } else if (continuous) {
                                        detected = if (preventDuplicates) {
                                            (detected + accepted).distinctBy { "${it.format}|${it.text}" }.takeLast(100)
                                        } else {
                                            (detected + accepted).takeLast(100)
                                        }
                                    }
                                },
                                onError = { error = it }
                            )
                        }
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        torch = !torch
                        cameraState?.takeIf { it.cameraInfo.hasFlashUnit() }?.cameraControl?.enableTorch(torch)
                    }) {
                        Icon(if (torch) Icons.Default.FlashOff else Icons.Default.FlashOn, contentDescription = null)
                    }
                    Text(if (torch) "فلش روشن" else "فلش خاموش")
                }
            } else {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("فعال‌کردن دوربین") }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (continuous) {
                Text("نتیجه‌های همین جلسه: ${detected.size}", fontWeight = FontWeight.Bold)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detected, key = { code -> "${code.format}:${code.text}:${detected.indexOf(code)}" }) { code ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onResult(code) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(code.format, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                Text(code.text, maxLines = 2)
                            }
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun bindCamera(
    activity: ComponentActivity,
    previewView: PreviewView,
    initialTorch: Boolean,
    onCamera: (androidx.camera.core.Camera) -> Unit,
    onCodes: (List<DecodedImageCode>) -> Unit,
    onError: (String) -> Unit
) {
    val future = ProcessCameraProvider.getInstance(activity)
    val executor = Executors.newSingleThreadExecutor()
    future.addListener({
        runCatching {
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val busy = AtomicBoolean(false)
            val scanner = BarcodeScanning.getClient()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { proxy ->
                val media = proxy.image
                if (media == null || !busy.compareAndSet(false, true)) {
                    proxy.close()
                    return@setAnalyzer
                }
                val input = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                scanner.process(input)
                    .addOnSuccessListener { codes ->
                        onCodes(codes.mapNotNull { barcode ->
                            val raw = barcode.rawValue ?: return@mapNotNull null
                            DecodedImageCode(raw, barcode.format.toFormatName())
                        })
                    }
                    .addOnFailureListener { onError(it.message ?: "خطا در تحلیل تصویر") }
                    .addOnCompleteListener {
                        busy.set(false)
                        proxy.close()
                    }
            }
            provider.unbindAll()
            val camera = provider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            if (initialTorch && camera.cameraInfo.hasFlashUnit()) camera.cameraControl.enableTorch(true)
            onCamera(camera)
        }.onFailure { onError(it.message ?: "راه‌اندازی دوربین ناموفق بود") }
    }, ContextCompat.getMainExecutor(activity))
}

private fun Int.toFormatName(): String = when (this) {
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> "QR_CODE"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> "CODE_128"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> "CODE_39"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> "EAN_13"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> "EAN_8"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> "UPC_A"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> "UPC_E"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> "ITF"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> "CODABAR"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> "PDF_417"
    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> "AZTEC"
    else -> "BARCODE"
}

private fun vibrateOnce(context: android.content.Context) {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(70L)
    }
}
