/*
 * App-QrCodeYar - اسکنر حرفه‌ای CameraX + ML Kit
 *
 * این Activity جای CaptureActivity قدیمی را گرفته است. مزایا:
 * - Preview واقعی CameraX داخل UI برنامه
 * - تشخیص چند QR/Barcode در یک فریم با ML Kit
 * - Torch و Zoom کنترل‌شده توسط CameraControl
 * - انتخاب یکی از چند نتیجه برای بازگشت به صفحه اصلی Scanner
 * - مدل ML Kit bundled؛ برای اولین اسکن نیاز به دانلود مدل جداگانه نیست
 */
package com.waxew.qrbarcode.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.journeyapps.barcodescanner.ScanContract
import com.waxew.qrbarcode.ui.theme.QrStudioTheme
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ModernScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prompt = intent.getStringExtra(ScanContract.EXTRA_PROMPT) ?: "کد را داخل کادر بگیر"
        val initialTorch = intent.getBooleanExtra(ScanContract.EXTRA_TORCH, false)
        val beep = intent.getBooleanExtra(ScanContract.EXTRA_BEEP, true)

        setContent {
            QrStudioTheme {
                ModernScannerScreen(
                    prompt = prompt,
                    initialTorch = initialTorch,
                    beepEnabled = beep,
                    onSelect = { code -> finishWithResult(code) },
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
private fun ModernScannerScreen(
    prompt: String,
    initialTorch: Boolean,
    beepEnabled: Boolean,
    onSelect: (DecodedImageCode) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torch by remember { mutableStateOf(initialTorch) }
    var zoom by remember { mutableStateOf(1f) }
    var maxZoom by remember { mutableStateOf(1f) }
    var detected by remember { mutableStateOf<List<DecodedImageCode>>(emptyList()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val tone = remember(beepEnabled) { if (beepEnabled) ToneGenerator(AudioManager.STREAM_MUSIC, 65) else null }
    DisposableEffect(tone) { onDispose { tone?.release() } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (!granted) errorText = "دسترسی دوربین داده نشد."
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.weight(1f))
                Text("اسکنر هوشمند", fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onCancel) { Text("بستن") }
            }
            Text(prompt, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)

            if (permissionGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.Black)
                ) {
                    LiveMlKitCamera(
                        modifier = Modifier.fillMaxSize(),
                        onCameraReady = { boundCamera ->
                            camera = boundCamera
                            val state = boundCamera.cameraInfo.zoomState.value
                            maxZoom = state?.maxZoomRatio ?: 1f
                            zoom = state?.zoomRatio ?: 1f
                            if (torch && boundCamera.cameraInfo.hasFlashUnit()) {
                                boundCamera.cameraControl.enableTorch(true)
                            }
                        },
                        onCodes = { codes ->
                            val before = detected.joinToString("|") { "${it.format}:${it.text}" }
                            detected = buildList {
                                addAll(codes)
                                addAll(detected.filterNot { old -> codes.any { it.text == old.text && it.format == old.format } })
                            }.take(20)
                            val after = detected.joinToString("|") { "${it.format}:${it.text}" }
                            if (before != after) tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
                        },
                        onError = { errorText = it }
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            torch = !torch
                            camera?.takeIf { it.cameraInfo.hasFlashUnit() }?.cameraControl?.enableTorch(torch)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (torch) Icons.Default.FlashOff else Icons.Default.FlashOn, contentDescription = null)
                        Text(if (torch) " خاموش" else " فلش")
                    }
                    IconButton(
                        onClick = {
                            zoom = (zoom - 0.5f).coerceAtLeast(1f)
                            camera?.cameraControl?.setZoomRatio(zoom)
                        },
                        enabled = zoom > 1f
                    ) { Icon(Icons.Default.ZoomOut, contentDescription = "کم کردن زوم") }
                    Text(String.format(Locale.US, "%.1f×", zoom), modifier = Modifier.align(Alignment.CenterVertically))
                    IconButton(
                        onClick = {
                            zoom = (zoom + 0.5f).coerceAtMost(maxZoom.coerceAtLeast(1f))
                            camera?.cameraControl?.setZoomRatio(zoom)
                        },
                        enabled = zoom < maxZoom
                    ) { Icon(Icons.Default.ZoomIn, contentDescription = "زیاد کردن زوم") }
                }
            } else {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("فعال‌کردن دوربین")
                }
            }

            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }

            Text(
                if (detected.isEmpty()) "کدها بعد از تشخیص اینجا نمایش داده می‌شوند."
                else "${detected.size} کد پیدا شد؛ یکی را انتخاب کنید.",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detected, key = { "${it.format}:${it.text}" }) { code ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(code) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(code.format, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(code.text, maxLines = 2, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun LiveMlKitCamera(
    modifier: Modifier,
    onCameraReady: (Camera) -> Unit,
    onCodes: (List<DecodedImageCode>) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(lifecycleOwner, previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var disposed = false
        val busy = AtomicBoolean(false)
        var lastSignature = ""
        var lastEmit = 0L

        providerFuture.addListener({
            if (disposed) return@addListener
            runCatching {
                provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(executor) { proxy ->
                    if (!busy.compareAndSet(false, true)) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = proxy.image
                    if (mediaImage == null) {
                        busy.set(false)
                        proxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener(executor) { barcodes ->
                            val codes = barcodes.mapNotNull { barcode ->
                                barcode.rawValue?.takeIf { it.isNotBlank() }?.let {
                                    DecodedImageCode(it, formatName(barcode.format))
                                }
                            }.distinctBy { "${it.format}:${it.text}" }
                            if (codes.isNotEmpty()) {
                                val signature = codes.joinToString("|") { "${it.format}:${it.text}" }
                                val now = System.currentTimeMillis()
                                if (signature != lastSignature || now - lastEmit > 1800L) {
                                    lastSignature = signature
                                    lastEmit = now
                                    previewView.post { if (!disposed) onCodes(codes) }
                                }
                            }
                        }
                        .addOnFailureListener(executor) { error ->
                            previewView.post { if (!disposed) onError(error.message ?: "تحلیل تصویر ناموفق بود.") }
                        }
                        .addOnCompleteListener(executor) {
                            busy.set(false)
                            proxy.close()
                        }
                }

                provider?.unbindAll()
                val bound = provider!!.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                previewView.post { if (!disposed) onCameraReady(bound) }
            }.onFailure { error ->
                previewView.post { if (!disposed) onError(error.message ?: "دوربین راه‌اندازی نشد.") }
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            provider?.unbindAll()
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun formatName(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR_CODE"
    Barcode.FORMAT_AZTEC -> "AZTEC"
    Barcode.FORMAT_CODABAR -> "CODABAR"
    Barcode.FORMAT_CODE_39 -> "CODE_39"
    Barcode.FORMAT_CODE_93 -> "CODE_93"
    Barcode.FORMAT_CODE_128 -> "CODE_128"
    Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    Barcode.FORMAT_EAN_8 -> "EAN_8"
    Barcode.FORMAT_EAN_13 -> "EAN_13"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_PDF417 -> "PDF_417"
    Barcode.FORMAT_UPC_A -> "UPC_A"
    Barcode.FORMAT_UPC_E -> "UPC_E"
    else -> "UNKNOWN"
}
