/*
 * App-QrCodeYar v1.9 - مرکز امکانات یکپارچه
 *
 * در این نسخه قابلیت‌هایی که قبلاً فقط زیرساخت داشتند به جریان واقعی UI وصل شده‌اند:
 * - Folder/Tag روی Room
 * - Start Page واقعی: خانه، اسکنر، مرکز 1.9
 * - Accent واقعی با recreate شدن Activity
 * - تنظیمات اسکنر متصل به Activity دوربین
 */
package com.waxew.qrbarcode.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.data.HistoryItem
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.scanner.V19ScannerActivity
import com.waxew.qrbarcode.v19.ProductLabelRenderer
import com.waxew.qrbarcode.v19.ProductLabelSpec
import com.waxew.qrbarcode.v19.SmartTemplateCatalog
import com.waxew.qrbarcode.v19.V19AppLock
import com.waxew.qrbarcode.v19.V19BackupManager
import com.waxew.qrbarcode.v19.V19LinkSecurity
import com.waxew.qrbarcode.v19.V19SettingsRepository
import com.waxew.qrbarcode.v20.V20Biometric
import androidx.fragment.app.FragmentActivity

enum class V19HubPage { HOME, ARCHIVE }

@Composable
fun QrCodeYarV19Root(
    activity: Activity,
    billingManager: BillingManager,
    preferences: PreferencesRepository,
    settings: V19SettingsRepository,
    appLock: V19AppLock
) {
    var showHub by remember { mutableStateOf(settings.startPage.startsWith("مرکز")) }
    var hubPage by remember { mutableStateOf(V19HubPage.HOME) }
    var unlocked by remember { mutableStateOf(!settings.appLockEnabled || !appLock.configured) }
    var unlockPin by remember { mutableStateOf("") }

    val startScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (!result.contents.isNullOrBlank()) {
            preferences.addHistory("SCAN-${result.formatName ?: "CAMERA"}", result.contents)
        }
    }
    LaunchedEffect(Unit) {
        if (settings.startPage == "اسکنر") {
            startScanner.launch(ScanOptions().setPrompt("اسکنر صفحه شروع").setOrientationLocked(false))
        }
    }

    val biometric = remember(activity) { (activity as? FragmentActivity)?.let { V20Biometric(it) } }
    LaunchedEffect(unlocked, settings.biometricUnlock) {
        if (!unlocked && settings.biometricUnlock && biometric?.available() == true) {
            biometric.authenticate(onSuccess = { unlocked = true })
        }
    }

    Box(Modifier.fillMaxSize()) {
        QrBarcodeApp(activity, billingManager, preferences)

        FloatingActionButton(
            onClick = { hubPage = V19HubPage.HOME; showHub = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
        ) { Text("2.0", fontWeight = FontWeight.Black) }

        if (showHub) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (hubPage) {
                    V19HubPage.HOME -> V19FeatureHub(
                        activity = activity,
                        preferences = preferences,
                        settings = settings,
                        appLock = appLock,
                        billingManager = billingManager,
                        onArchive = { hubPage = V19HubPage.ARCHIVE },
                        onClose = { showHub = false }
                    )
                    V19HubPage.ARCHIVE -> ArchiveManager(
                        preferences = preferences,
                        onBack = { hubPage = V19HubPage.HOME }
                    )
                }
            }
        }
    }

    if (!unlocked) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("قفل QR یار") },
            text = {
                OutlinedTextField(
                    value = unlockPin,
                    onValueChange = { unlockPin = it.filter(Char::isDigit).take(8) },
                    label = { Text("PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (appLock.verify(unlockPin)) {
                        unlocked = true
                        unlockPin = ""
                    }
                }) { Text("باز کردن") }
            },
            dismissButton = {
                if (settings.biometricUnlock && biometric?.available() == true) {
                    TextButton(onClick = { biometric.authenticate(onSuccess = { unlocked = true }) }) { Text("Biometric") }
                }
            }
        )
    }
}

@Composable
private fun V19FeatureHub(
    activity: Activity,
    preferences: PreferencesRepository,
    settings: V19SettingsRepository,
    appLock: V19AppLock,
    billingManager: BillingManager,
    onArchive: () -> Unit,
    onClose: () -> Unit
) {
    var testUrl by remember { mutableStateOf("https://example.com") }
    var pin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val premiumState by billingManager.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("مرکز امکانات نسخه 2.0", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("قابلیت‌های 1.2 تا 2.0 یکپارچه شده‌اند")
                }
                TextButton(onClick = onClose) { Text("بستن") }
            }
        }

        item {
            FeatureCard("🎨 QR Designer", "QR Studio اصلی همچنان شامل گرادیان، لوگو، Finder، قاب، Undo/Redo و کنترل خوانایی است.")
        }
        item { V20SmartTemplatePanel(preferences, premiumState.active) }
        item { V20LabelStudioPanel(preferences, premiumState.active) }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔐 امنیت لینک", fontWeight = FontWeight.Black)
                    OutlinedTextField(testUrl, { testUrl = it }, Modifier.fillMaxWidth(), label = { Text("URL برای بررسی") })
                    val assessment = V19LinkSecurity.assess(testUrl)
                    Text("امتیاز ریسک: ${assessment.score}/100")
                    Text(if (assessment.reasons.isEmpty()) "نشانه مشکوک مشخصی پیدا نشد." else assessment.reasons.joinToString(" • "))
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📷 تنظیمات اسکنر حرفه‌ای", fontWeight = FontWeight.Black)
                    ToggleRow("صدای اسکن", settings.scannerBeep) { settings.scannerBeep = it }
                    ToggleRow("لرزش بعد از اسکن", settings.scannerVibrate) { settings.scannerVibrate = it }
                    ToggleRow("اسکن متوالی", settings.continuousScan) { settings.continuousScan = it }
                    ToggleRow("جلوگیری از نتیجه تکراری", settings.preventDuplicates) { settings.preventDuplicates = it }
                    ToggleRow("تأیید قبل از باز کردن لینک", settings.confirmBeforeOpeningLinks) { settings.confirmBeforeOpeningLinks = it }
                    Text("این گزینه‌ها مستقیم به Scanner Activity متصل هستند.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🗂️ آرشیو، Folder و Tag", fontWeight = FontWeight.Black)
                    Text("Folder و Tag داخل Room ذخیره می‌شوند و با بروزرسانی برنامه باقی می‌مانند.")
                    Text("رکوردهای فعلی: ${preferences.history().size} • پوشه‌ها: ${preferences.folders().size}")
                    Button(onClick = onArchive, modifier = Modifier.fillMaxWidth()) { Text("مدیریت آرشیو") }
                    OutlinedButton(onClick = {
                        val json = V19BackupManager.buildJson(preferences.appContext, preferences.history(), settings)
                        val file = V19BackupManager.saveToCache(preferences.appContext, json)
                        message = "بکاپ محلی ساخته شد: ${file.name}"
                    }, modifier = Modifier.fillMaxWidth()) { Text("ساخت بکاپ JSON") }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✨ شخصی‌سازی واقعی", fontWeight = FontWeight.Black)
                    ToggleRow("حالت Compact", settings.compactMode) { settings.compactMode = it }
                    Text("Accent فعلی: ${settings.accentName}")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("صورتی یاسی", "سبز نعنایی", "آبی آسمانی").forEach { accent ->
                            OutlinedButton(onClick = {
                                settings.accentName = accent
                                activity.recreate()
                            }) { Text(accent.substringBefore(' ')) }
                        }
                    }
                    Text("صفحه شروع: ${settings.startPage}")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("خانه", "اسکنر", "مرکز 2.0").forEach { page ->
                            OutlinedButton(onClick = { settings.startPage = page }) { Text(page) }
                        }
                    }
                    Text("صفحه شروع در اجرای بعدی برنامه اعمال می‌شود.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔒 قفل برنامه با PIN", fontWeight = FontWeight.Black)
                    OutlinedTextField(
                        pin,
                        { pin = it.filter(Char::isDigit).take(8) },
                        Modifier.fillMaxWidth(),
                        label = { Text("PIN چهار تا هشت رقمی") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    val biometricAvailable = (activity as? FragmentActivity)?.let { V20Biometric(it).available() } == true
                    ToggleRow("بازکردن با Biometric / قفل دستگاه", settings.biometricUnlock) { enabled ->
                        if (enabled && !appLock.configured) message = "برای فعال‌کردن Biometric ابتدا PIN برنامه را تنظیم کنید."
                        else if (enabled && !biometricAvailable) message = "Biometric یا قفل امن دستگاه در دسترس نیست."
                        else settings.biometricUnlock = enabled
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (appLock.setPin(pin)) {
                                settings.appLockEnabled = true
                                message = "قفل PIN فعال شد."
                                pin = ""
                            } else message = "PIN باید 4 تا 8 رقم باشد."
                        }) { Text("فعال‌سازی") }
                        OutlinedButton(onClick = {
                            appLock.clear()
                            settings.appLockEnabled = false
                            settings.biometricUnlock = false
                            message = "قفل برنامه غیرفعال شد."
                        }) { Text("غیرفعال") }
                    }
                }
            }
        }

        item { V20CloudPanel(preferences, premiumState.active) }
        item { FeatureCard("📚 Batch & Print", "CSV/TXT/XLSX چندستونه، Mapping ستون، PNG، ZIP و PDF لیبل A4/A5 با چیدمان سفارشی فعال هستند.") }
        item { FeatureCard("🛍️ ابزارهای فروشگاهی", "لیبل کالا، QR منو، Wi-Fi، شبکه اجتماعی و موقعیت در جریان‌های سازنده موجودند.") }

        if (message.isNotBlank()) item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(message, Modifier.fillMaxWidth().padding(14.dp), textAlign = TextAlign.Center)
            }
        }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
private fun ArchiveManager(preferences: PreferencesRepository, onBack: () -> Unit) {
    var revision by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var folderFilter by remember { mutableStateOf("همه") }
    var editing by remember { mutableStateOf<HistoryItem?>(null) }
    var folder by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val all = remember(revision) { preferences.history() }
    val folders = remember(all) { listOf("همه") + preferences.folders() }
    val visible = remember(all, query, folderFilter) {
        all.filter { item ->
            (query.isBlank() || item.payload.contains(query, true) || item.tags.contains(query, true) || item.folder.contains(query, true)) &&
                (folderFilter == "همه" || item.folder == folderFilter)
        }
    }

    editing?.let { item ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("پوشه و برچسب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(folder, { folder = it.take(40) }, label = { Text("پوشه") }, singleLine = true)
                    OutlinedTextField(tags, { tags = it.take(300) }, label = { Text("Tagها با , جدا شوند") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    preferences.updateArchiveMetadata(item.createdAt, folder, tags)
                    revision++
                    editing = null
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("لغو") } }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("مدیریت آرشیو", Modifier.weight(1f), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("بازگشت") }
            }
        }
        item {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("جستجو در محتوا، پوشه یا Tag") })
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                folders.forEach { name ->
                    OutlinedButton(onClick = { folderFilter = name }) { Text(if (name == folderFilter) "✓ $name" else name) }
                }
            }
        }
        if (visible.isEmpty()) item { Text("رکوردی برای این فیلتر وجود ندارد.") }
        items(visible, key = { it.createdAt }) { item ->
            Card(
                Modifier.fillMaxWidth().clickable {
                    editing = item
                    folder = item.folder
                    tags = item.tags
                },
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.kind, fontWeight = FontWeight.Bold)
                    Text(item.payload, maxLines = 2)
                    Text("پوشه: ${item.folder.ifBlank { "بدون پوشه" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Tag: ${item.tags.ifBlank { "بدون برچسب" }}", style = MaterialTheme.typography.bodySmall)
                    Text("برای ویرایش پوشه و Tag روی رکورد بزنید.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun FeatureCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Black)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
