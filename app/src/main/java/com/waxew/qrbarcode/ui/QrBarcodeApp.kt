package com.waxew.qrbarcode.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.waxew.qrbarcode.BuildConfig
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.billing.PremiumState
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.export.ExportManager
import com.waxew.qrbarcode.generator.CodeGenerator
import com.waxew.qrbarcode.generator.GeneratedCode
import com.waxew.qrbarcode.generator.ModuleStyle
import com.waxew.qrbarcode.update.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Screen(val title: String) {
    HOME("خانه"),
    QR("ساخت QR"),
    BARCODE("ساخت Barcode"),
    SCANNER("اسکنر"),
    TEMPLATES("طرح‌های آماده"),
    HISTORY("تاریخچه"),
    PREMIUM("اشتراک حرفه‌ای"),
    SETTINGS("تنظیمات"),
    ABOUT_US("درباره ما"),
    CONTACT("تماس با ما"),
    ABOUT_APP("درباره نرم افزار")
}

private enum class QrKind(val title: String, val emoji: String, val hint: String) {
    URL("لینک", "🔗", "https://example.com"),
    TEXT("متن", "💬", "متن دلخواه"),
    WIFI("Wi-Fi", "📶", "SSID|PASSWORD"),
    EMAIL("ایمیل", "✉️", "name@example.com"),
    PHONE("تلفن", "📞", "+49123456789"),
    SMS("پیامک", "💌", "+49123456789|متن پیام")
}

private data class HomeAction(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrBarcodeApp(
    activity: Activity,
    billingManager: BillingManager,
    preferences: PreferencesRepository
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        val premiumState by billingManager.state.collectAsStateCompat()
        var screen by remember { mutableStateOf(Screen.HOME) }
        var updateInfo by remember { mutableStateOf<com.waxew.qrbarcode.update.UpdateInfo?>(null) }

        LaunchedEffect(Unit) {
            updateInfo = withContext(Dispatchers.IO) { UpdateChecker.check() }
        }

        updateInfo?.let { info ->
            AlertDialog(
                onDismissRequest = { updateInfo = null },
                title = { Text("نسخه جدید رسید 🎉") },
                text = { Text("نسخه ${info.versionName}\n${info.changelog}") },
                confirmButton = {
                    Button(onClick = { UpdateChecker.openDownload(activity, info) }) { Text("دریافت بروزرسانی") }
                },
                dismissButton = {
                    TextButton(onClick = { updateInfo = null }) { Text("بعداً") }
                }
            )
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerHeader(premiumState.active)
                    DrawerItem("خانه", Icons.Default.Home, screen == Screen.HOME) { screen = Screen.HOME }
                    DrawerItem("اشتراک حرفه‌ای", Icons.Default.Paid, screen == Screen.PREMIUM) { screen = Screen.PREMIUM }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DrawerItem("تنظیمات", Icons.Default.Settings, screen == Screen.SETTINGS) { screen = Screen.SETTINGS }
                    DrawerItem("معرفی به دوستان", Icons.Default.Share, false) { shareApp(activity) }
                    DrawerItem("درباره ما", Icons.Default.Info, screen == Screen.ABOUT_US) { screen = Screen.ABOUT_US }
                    DrawerItem("تماس با ما", Icons.Default.ContactSupport, screen == Screen.CONTACT) { screen = Screen.CONTACT }
                    DrawerItem("درباره نرم افزار", Icons.Default.QrCode, screen == Screen.ABOUT_APP) { screen = Screen.ABOUT_APP }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(screen.title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "منوی همبرگری")
                            }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbar) }
            ) { inner ->
                Box(Modifier.fillMaxSize().padding(inner)) {
                    when (screen) {
                        Screen.HOME -> HomeScreen(onOpen = { screen = it })
                        Screen.QR -> QrMakerScreen(
                            isPremium = premiumState.active,
                            preferences = preferences,
                            onNeedPremium = { screen = Screen.PREMIUM },
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.BARCODE -> BarcodeMakerScreen(
                            isPremium = premiumState.active,
                            preferences = preferences,
                            onNeedPremium = { screen = Screen.PREMIUM },
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.SCANNER -> ScannerScreen(preferences)
                        Screen.TEMPLATES -> TemplatesScreen(
                            isPremium = premiumState.active,
                            onOpenMaker = { screen = Screen.QR },
                            onNeedPremium = { screen = Screen.PREMIUM }
                        )
                        Screen.HISTORY -> HistoryScreen(preferences)
                        Screen.PREMIUM -> PremiumScreen(activity, billingManager, premiumState)
                        Screen.SETTINGS -> SettingsScreen(preferences)
                        Screen.ABOUT_US -> AboutUsScreen()
                        Screen.CONTACT -> ContactScreen()
                        Screen.ABOUT_APP -> AboutAppScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (Screen) -> Unit) {
    val actions = remember {
        listOf(
            HomeAction("ساخت QR", "لینک، متن، Wi-Fi و بیشتر", "🧩", Screen.QR),
            HomeAction("ساخت Barcode", "Code 128، EAN، PDF417...", "🏷️", Screen.BARCODE),
            HomeAction("اسکن کد", "QR و بارکد را سریع بخوان", "🔎", Screen.SCANNER),
            HomeAction("طرح‌های آماده", "قالب‌های کیوت و کاربردی", "🎀", Screen.TEMPLATES),
            HomeAction("تاریخچه", "کدهای اخیرت را ببین", "🕘", Screen.HISTORY),
            HomeAction("حرفه‌ای", "خروجی HD، PDF و SVG", "👑", Screen.PREMIUM)
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard()
        }
        item {
            Text("چی می‌خوای بسازی؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(470.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(actions) { action -> HomeActionCard(action) { onOpen(action.screen) } }
            }
        }
        item {
            CuteTip()
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) { Text("(｡•̀ᴗ-)✧", fontSize = 18.sp) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("استودیوی کوچولوی کدها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("بساز، خوشگلش کن، اسکن کن و خروجی بگیر.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun HomeActionCard(action: HomeAction, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(142.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(action.emoji, fontSize = 30.sp)
            Text(action.title, fontWeight = FontWeight.ExtraBold)
            Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CuteTip() {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🐣", fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Text("QRهای حرفه‌ای قبل از خروجی با برچسب Pro مشخص می‌شوند؛ ساخت و تست ساده همیشه رایگان می‌ماند.")
        }
    }
}

@Composable
private fun QrMakerScreen(
    isPremium: Boolean,
    preferences: PreferencesRepository,
    onNeedPremium: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var kind by remember { mutableStateOf(QrKind.URL) }
    var input by remember { mutableStateOf("https://") }
    var style by remember { mutableStateOf(ModuleStyle.CLASSIC) }
    var fg by remember { mutableStateOf(AndroidColor.rgb(42, 34, 55)) }
    val bg = AndroidColor.WHITE
    val payload = remember(kind, input) { buildQrPayload(kind, input) }
    val generated = remember(payload, style, fg) {
        runCatching { CodeGenerator.qr(payload, style = style, foreground = fg, background = bg) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CodePreview(generated.getOrNull(), generated.exceptionOrNull()?.message)
        }
        item {
            SectionTitle("نوع QR")
            ChipRow {
                QrKind.entries.forEach { item ->
                    ChoiceChip("${item.emoji} ${item.title}", selected = kind == item) {
                        kind = item
                        input = if (item == QrKind.URL) "https://" else ""
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = if (kind == QrKind.TEXT) 3 else 1,
                label = { Text(kind.title) },
                placeholder = { Text(kind.hint) }
            )
        }
        item {
            SectionTitle("استایل ماژول‌ها")
            ChipRow {
                ModuleStyle.entries.forEach { item ->
                    ChoiceChip(
                        label = item.title + if (item.premium) " 👑" else "",
                        selected = style == item
                    ) { style = item }
                }
            }
        }
        item {
            SectionTitle("رنگ")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    AndroidColor.rgb(42, 34, 55),
                    AndroidColor.rgb(74, 84, 130),
                    AndroidColor.rgb(116, 71, 111),
                    AndroidColor.rgb(55, 112, 98)
                ).forEach { color ->
                    ColorDot(color, selected = fg == color) { fg = color }
                }
            }
        }
        item {
            ExportPanel(
                professionalDesign = style.premium,
                isPremium = isPremium,
                onFreePng = {
                    generated.getOrNull()?.let {
                        if (style.premium && !isPremium) onNeedPremium()
                        else {
                            ExportManager.savePng(context, it.bitmap, "qr_${System.currentTimeMillis()}")
                            preferences.addHistory("QR", input)
                            onMessage("PNG ذخیره شد 🌸")
                        }
                    }
                },
                onPremiumPng = {
                    if (!isPremium) onNeedPremium() else runCatching {
                        val hd = CodeGenerator.qr(payload, size = 2048, style = style, foreground = fg, background = bg)
                        ExportManager.savePng(context, hd.bitmap, "qr_hd_${System.currentTimeMillis()}")
                        preferences.addHistory("QR-HD", input)
                    }.onSuccess { onMessage("PNG با کیفیت HD ذخیره شد ✨") }.onFailure { onMessage(it.message ?: "خطا در خروجی") }
                },
                onPdf = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.savePdf(context, it.bitmap, "qr_${System.currentTimeMillis()}")
                        preferences.addHistory("QR-PDF", input)
                        onMessage("PDF ذخیره شد")
                    }
                },
                onSvg = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.saveSvg(context, it.matrix, it.foreground, it.background, "qr_${System.currentTimeMillis()}")
                        preferences.addHistory("QR-SVG", input)
                        onMessage("SVG ذخیره شد")
                    }
                }
            )
        }
    }
}

@Composable
private fun BarcodeMakerScreen(
    isPremium: Boolean,
    preferences: PreferencesRepository,
    onNeedPremium: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val formats = remember {
        listOf(
            "Code 128" to BarcodeFormat.CODE_128,
            "Code 39" to BarcodeFormat.CODE_39,
            "EAN-13" to BarcodeFormat.EAN_13,
            "EAN-8" to BarcodeFormat.EAN_8,
            "UPC-A" to BarcodeFormat.UPC_A,
            "ITF" to BarcodeFormat.ITF,
            "Codabar" to BarcodeFormat.CODABAR,
            "Data Matrix" to BarcodeFormat.DATA_MATRIX,
            "PDF417" to BarcodeFormat.PDF_417,
            "Aztec" to BarcodeFormat.AZTEC
        )
    }
    var selected by remember { mutableStateOf(formats.first()) }
    var input by remember { mutableStateOf("123456789012") }
    val generated = remember(input, selected) {
        runCatching { CodeGenerator.barcode(input, selected.second) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CodePreview(generated.getOrNull(), generated.exceptionOrNull()?.message, barcode = true) }
        item {
            SectionTitle("نوع بارکد")
            ChipRow {
                formats.forEach { item -> ChoiceChip(item.first, selected == item) { selected = item } }
            }
        }
        item {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مقدار بارکد") },
                supportingText = { Text("EAN و UPC فقط اعداد و طول استاندارد خودشان را قبول می‌کنند.") }
            )
        }
        item {
            ExportPanel(
                professionalDesign = false,
                isPremium = isPremium,
                onFreePng = {
                    generated.getOrNull()?.let {
                        ExportManager.savePng(context, it.bitmap, "barcode_${System.currentTimeMillis()}")
                        preferences.addHistory(selected.first, input)
                        onMessage("PNG ذخیره شد 🏷️")
                    }
                },
                onPremiumPng = {
                    if (!isPremium) onNeedPremium() else runCatching {
                        val hd = CodeGenerator.barcode(input, selected.second, width = 2400, height = 900)
                        ExportManager.savePng(context, hd.bitmap, "barcode_hd_${System.currentTimeMillis()}")
                        preferences.addHistory("${selected.first}-HD", input)
                    }.onSuccess { onMessage("PNG با کیفیت چاپ ذخیره شد") }.onFailure { onMessage(it.message ?: "خطا") }
                },
                onPdf = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.savePdf(context, it.bitmap, "barcode_${System.currentTimeMillis()}")
                        onMessage("PDF ذخیره شد")
                    }
                },
                onSvg = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.saveSvg(context, it.matrix, it.foreground, it.background, "barcode_${System.currentTimeMillis()}")
                        onMessage("SVG ذخیره شد")
                    }
                }
            )
        }
    }
}

@Composable
private fun ScannerScreen(preferences: PreferencesRepository) {
    var lastResult by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            lastResult = result.contents
            preferences.addHistory("SCAN", result.contents)
        }
    }
    val options = remember {
        ScanOptions()
            .setPrompt("کد را داخل کادر بگیر ✨")
            .setBeepEnabled(true)
            .setOrientationLocked(false)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("🔍", fontSize = 72.sp)
        Text("اسکنر QR و Barcode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("دوربین باز می‌شود و نتیجه بعد از خواندن کد همین‌جا نمایش داده می‌شود.", textAlign = TextAlign.Center)
        Button(onClick = { launcher.launch(options) }) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("شروع اسکن")
        }
        lastResult?.let {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("نتیجه", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun TemplatesScreen(isPremium: Boolean, onOpenMaker: () -> Unit, onNeedPremium: () -> Unit) {
    val templates = listOf(
        Triple("ساده و تمیز", "🌱", false),
        Triple("Wi-Fi Cute", "☁️", true),
        Triple("کارت شبکه اجتماعی", "💖", true),
        Triple("لیبل محصول", "🎁", true),
        Triple("کارت ویزیت QR", "🪪", true),
        Triple("منوی رستوران", "🍰", true)
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(templates) { item ->
            Card(
                modifier = Modifier.height(158.dp).clickable {
                    if (item.third && !isPremium) onNeedPremium() else onOpenMaker()
                },
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.second, fontSize = 34.sp)
                    Text(item.first, fontWeight = FontWeight.ExtraBold)
                    Text(if (item.third) "PRO 👑" else "رایگان", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(preferences: PreferencesRepository) {
    val history = remember { preferences.history() }
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🫧", fontSize = 60.sp)
                Text("هنوز چیزی اینجا نیست")
            }
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(history.size) { index ->
            val item = history[index]
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.kind, fontWeight = FontWeight.Bold)
                        Text(item.payload, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(formatTime(item.createdAt), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PremiumScreen(activity: Activity, billingManager: BillingManager, premiumState: PremiumState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Text(if (premiumState.active) "👑✨" else "👑", fontSize = 72.sp) }
        item {
            Text(
                if (premiumState.active) "اشتراک حرفه‌ای فعاله" else "اشتراک حرفه‌ای هفتگی",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                "خروجی ساده رایگان می‌ماند. فقط وقتی طرح یا فایل وارد سطح حرفه‌ای شود، اشتراک لازم است.",
                textAlign = TextAlign.Center
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumFeature("PNG با کیفیت چاپ و HD")
                PremiumFeature("PDF و SVG برداری")
                PremiumFeature("استایل‌های نقطه‌ای، گرد و حبابی")
                PremiumFeature("قالب‌های حرفه‌ای و لیبل محصول")
                PremiumFeature("خروجی‌های بدون محدودیت در مدت اشتراک")
            }
        }
        item {
            if (premiumState.active) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(20.dp)) {
                    Text("فعال ✓", Modifier.padding(horizontal = 30.dp, vertical = 14.dp), fontWeight = FontWeight.Black)
                }
            } else {
                Button(
                    onClick = { billingManager.launch(activity) },
                    enabled = premiumState.available,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (premiumState.available) "فعال‌سازی ${premiumState.priceText}" else "در انتظار تنظیم محصول فروشگاه")
                }
            }
        }
        premiumState.message?.let { item { Text(it, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary) } }
        item {
            Text(
                "محصول فروشگاه: ${BillingManager.WEEKLY_PRODUCT_ID}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsScreen(preferences: PreferencesRepository) {
    var notifications by remember { mutableStateOf(preferences.notificationsEnabled) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsRow("اعلان‌ها", "خبر بروزرسانی‌ها و قابلیت‌های جدید", Icons.Default.Notifications) {
                Switch(
                    checked = notifications,
                    onCheckedChange = {
                        notifications = it
                        preferences.notificationsEnabled = it
                    }
                )
            }
        }
        item { SettingsRow("حالت نمایش", "هماهنگ با روشن/تیره بودن گوشی", Icons.Default.DarkMode) { Text("خودکار") } }
        item { SettingsRow("نسخه برنامه", "Version ${BuildConfig.VERSION_NAME}", Icons.Default.Verified) { Text("${BuildConfig.VERSION_CODE}") } }
    }
}

@Composable
private fun AboutUsScreen() = CenterInfo {
    Text("گروه توسعه و برنامه نویسی AS Team", fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text("تمامی حقوق مربوط به این برنامه انحصاری میباشد", textAlign = TextAlign.Center)
}

@Composable
private fun ContactScreen() = CenterInfo {
    Text("گروه توسعه و برنامه نویسی AS Team", fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    Spacer(Modifier.height(14.dp))
    Text("ایمیل پشتیبانی", fontWeight = FontWeight.Bold)
    Text("as.team.support@gmail.com")
}

@Composable
private fun AboutAppScreen() = CenterInfo {
    Text("QR ساز و Barcode ساز", fontWeight = FontWeight.Black, fontSize = 22.sp)
    Spacer(Modifier.height(12.dp))
    Text("ساخت، طراحی، اسکن و خروجی انواع QR و Barcode با امکانات ساده رایگان و ابزارهای حرفه‌ای اشتراکی.", textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text("نسخه ${BuildConfig.VERSION_NAME}")
}

@Composable
private fun CenterInfo(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
private fun DrawerHeader(premium: Boolean) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("🧸 QR ساز", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(if (premium) "حالت حرفه‌ای فعاله 👑" else "کدهای کوچولو، خروجی‌های جدی ✨", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        icon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}

@Composable
private fun CodePreview(code: GeneratedCode?, error: String?, barcode: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(if (barcode) 210.dp else 320.dp).padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                code != null -> Image(
                    bitmap = code.bitmap.asImageBitmap(),
                    contentDescription = "پیش نمایش کد",
                    modifier = Modifier.fillMaxSize()
                )
                error != null -> Text(error, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                else -> Text("محتوا را وارد کن")
            }
        }
    }
}

@Composable
private fun ExportPanel(
    professionalDesign: Boolean,
    isPremium: Boolean,
    onFreePng: () -> Unit,
    onPremiumPng: () -> Unit,
    onPdf: () -> Unit,
    onSvg: () -> Unit
) {
    Card(shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("خروجی", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (professionalDesign && !isPremium) Text("PRO 👑", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Text(
                if (professionalDesign && !isPremium) "این طرح حرفه‌ای است؛ خروجی آن با اشتراک هفتگی باز می‌شود." else "PNG استاندارد رایگان است؛ فرمت‌های چاپی و HD حرفه‌ای هستند.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onFreePng, modifier = Modifier.fillMaxWidth()) {
                Text(if (professionalDesign && !isPremium) "PNG استاندارد • نیازمند Pro" else "PNG استاندارد • رایگان")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPremiumPng, modifier = Modifier.weight(1f)) { Text("PNG HD 👑") }
                OutlinedButton(onClick = onPdf, modifier = Modifier.weight(1f)) { Text("PDF 👑") }
                OutlinedButton(onClick = onSvg, modifier = Modifier.weight(1f)) { Text("SVG 👑") }
            }
        }
    }
}

@Composable
private fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(label, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ColorDot(color: Int, selected: Boolean, onClick: () -> Unit) {
    val composeColor = Color(color)
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(composeColor)
            .then(if (selected) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun PremiumFeature(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(text)
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: @Composable () -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            trailing()
        }
    }
}

private fun buildQrPayload(kind: QrKind, input: String): String {
    if (input.isBlank()) return ""
    return when (kind) {
        QrKind.URL, QrKind.TEXT -> input
        QrKind.EMAIL -> "mailto:$input"
        QrKind.PHONE -> "tel:$input"
        QrKind.WIFI -> {
            val parts = input.split("|", limit = 2)
            val ssid = parts.getOrElse(0) { "" }
            val password = parts.getOrElse(1) { "" }
            "WIFI:T:WPA;S:${escapeWifi(ssid)};P:${escapeWifi(password)};;"
        }
        QrKind.SMS -> {
            val parts = input.split("|", limit = 2)
            val phone = parts.getOrElse(0) { "" }
            val body = parts.getOrElse(1) { "" }
            "SMSTO:$phone:$body"
        }
    }
}

private fun escapeWifi(value: String): String = value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace(":", "\\:")

private fun shareApp(activity: Activity) {
    val text = "QR ساز و Barcode ساز AS Team\nhttps://github.com/waxew/App-QrCodeYar"
    activity.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "معرفی به دوستان"
        )
    )
}

private fun formatTime(time: Long): String = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(time))

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateCompat(): androidx.compose.runtime.State<T> =
    collectAsState()
