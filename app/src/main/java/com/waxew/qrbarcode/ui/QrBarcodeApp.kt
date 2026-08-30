/*
 * App-QrCodeYar - رابط کاربری اصلی برنامه
 *
 * این فایل صفحه‌های Compose، Drawer راست‌چین، ناوبری داخلی، QR Studio، Barcode، Scanner،
 * Batch Tools، تاریخچه، تنظیمات، اشتراک و صفحه‌های اطلاعاتی را به هم متصل می‌کند.
 *
 * قانون مهم ناوبری: رفتن به هر صفحه باید از navigateTo() انجام شود تا Back اندروید ابتدا
 * به صفحه قبلی برگردد و فقط از HOME باعث خروج برنامه شود.
 */
package com.waxew.qrbarcode.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.waxew.qrbarcode.BuildConfig
import com.waxew.qrbarcode.batch.BatchInputReader
import com.waxew.qrbarcode.batch.BatchTable
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.billing.PremiumState
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.export.ExportManager
import com.waxew.qrbarcode.export.LabelPdfSpec
import com.waxew.qrbarcode.generator.CodeGenerator
import com.waxew.qrbarcode.generator.FinderStyle
import com.waxew.qrbarcode.generator.FrameStyle
import com.waxew.qrbarcode.generator.GeneratedCode
import com.waxew.qrbarcode.generator.GradientDirection
import com.waxew.qrbarcode.generator.LogoShape
import com.waxew.qrbarcode.generator.ModuleStyle
import com.waxew.qrbarcode.generator.QrDesign
import com.waxew.qrbarcode.scanner.DecodedImageCode
import com.waxew.qrbarcode.scanner.ImageCodeDecoder
import com.waxew.qrbarcode.scanner.LinkRiskLevel
import com.waxew.qrbarcode.scanner.ScanSafetyAnalyzer
import com.waxew.qrbarcode.update.UpdateChecker
import com.waxew.qrbarcode.util.NumberFormatter
import com.waxew.qrbarcode.v19.V19SettingsRepository
import com.waxew.qrbarcode.v20.V20DesignPreset
import com.waxew.qrbarcode.v20.V20DesignPresetStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// همه مقصدهای داخلی برنامه. title عنوان TopAppBar است.
private enum class Screen(val title: String) {
    HOME("خانه"),
    QR("استودیوی QR"),
    BARCODE("ساخت Barcode"),
    SCANNER("اسکنر"),
    BATCH("ساخت گروهی"),
    TEMPLATES("طرح‌های آماده"),
    HISTORY("تاریخچه"),
    PREMIUM("اشتراک حرفه‌ای"),
    SETTINGS("تنظیمات"),
    ABOUT_US("درباره ما"),
    CONTACT("تماس با ما"),
    ABOUT_APP("درباره نرم افزار")
}

// انواع payload قابل ساخت. hint قالب ورودی را به کاربر توضیح می‌دهد.
private enum class QrKind(val title: String, val emoji: String, val hint: String) {
    URL("لینک", "🔗", "https://example.com"),
    TEXT("متن", "💬", "متن دلخواه"),
    WIFI("Wi-Fi", "📶", "SSID|PASSWORD"),
    EMAIL("ایمیل", "✉️", "name@example.com"),
    PHONE("تلفن", "📞", "+989121234567"),
    SMS("پیامک", "💌", "+989121234567|متن پیام"),
    VCARD("مخاطب", "🪪", "نام|تلفن|ایمیل|سازمان|وبسایت"),
    EVENT("رویداد", "📅", "عنوان|20260901T090000|20260901T100000|مکان"),
    GEO("موقعیت", "📍", "35.6892,51.3890"),
    SOCIAL("شبکه اجتماعی", "💗", "https://instagram.com/example")
}

private data class HomeAction(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val screen: Screen
)

// State قابل Undo/Redo برای گزینه‌های طراحی QR.
private data class QrDesignState(
    val moduleStyle: ModuleStyle = ModuleStyle.CLASSIC,
    val finderStyle: FinderStyle = FinderStyle.CLASSIC,
    val foreground: Int = AndroidColor.rgb(42, 34, 55),
    val finderForeground: Int = AndroidColor.rgb(42, 34, 55),
    val gradientEnd: Int = AndroidColor.rgb(129, 103, 180),
    val gradientEnabled: Boolean = false,
    val gradientDirection: GradientDirection = GradientDirection.DIAGONAL,
    val moduleScale: Float = 1f,
    val background: Int = AndroidColor.WHITE,
    val transparentBackground: Boolean = false,
    val backgroundImageUri: String? = null,
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val frameText: String = "",
    val logoUri: String? = null,
    val logoShape: LogoShape = LogoShape.ROUNDED,
    val logoBorderColor: Int = AndroidColor.WHITE
) {
    fun professional(): Boolean =
        moduleStyle.premium || finderStyle.premium || gradientEnabled || transparentBackground ||
            frameStyle.premium || logoUri != null || backgroundImageUri != null || background != AndroidColor.WHITE ||
            foreground != AndroidColor.rgb(42, 34, 55) || finderForeground != foreground || moduleScale != 1f ||
            logoShape != LogoShape.ROUNDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrBarcodeApp(
    activity: Activity,
    billingManager: BillingManager,
    preferences: PreferencesRepository
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val context = LocalContext.current
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        val premiumState by billingManager.state.collectAsStateCompat()

        var screen by remember { mutableStateOf(Screen.HOME) }
        val backStack = remember { mutableStateListOf<Screen>() }
        var updateInfo by remember { mutableStateOf<com.waxew.qrbarcode.update.UpdateInfo?>(null) }

        // State پروفایل Drawer از SharedPreferences آغاز می‌شود و بعد از ویرایش همان لحظه UI را آپدیت می‌کند.
        var profileName by remember { mutableStateOf(preferences.profileName) }
        var profileImageUri by remember { mutableStateOf(preferences.profileImageUri) }
        var showNameDialog by remember { mutableStateOf(false) }
        var editingName by remember { mutableStateOf(profileName) }

        val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                profileImageUri = uri.toString()
                preferences.profileImageUri = profileImageUri
            }
        }
        val profileBitmap = remember(profileImageUri) { loadLocalBitmap(context, profileImageUri, 640) }

        fun navigateTo(target: Screen, clearHistory: Boolean = false) {
            if (target == screen) {
                scope.launch { drawerState.close() }
                return
            }
            if (clearHistory) backStack.clear() else backStack.add(screen)
            screen = target
            scope.launch { drawerState.close() }
        }

        BackHandler(enabled = drawerState.isOpen || screen != Screen.HOME) {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else if (backStack.isNotEmpty()) {
                screen = backStack.removeAt(backStack.lastIndex)
            } else {
                screen = Screen.HOME
            }
        }

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
                dismissButton = { TextButton(onClick = { updateInfo = null }) { Text("بعداً") } }
            )
        }

        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("نام نمایشی") },
                text = {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it.take(40) },
                        singleLine = true,
                        label = { Text("نام کاربر") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        profileName = editingName.trim().ifBlank { "کاربر" }
                        preferences.profileName = profileName
                        showNameDialog = false
                    }) { Text("ذخیره") }
                },
                dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("لغو") } }
            )
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.88f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        item {
                            DrawerProfileHeader(
                                premium = premiumState.active,
                                profileName = profileName,
                                profileBitmap = profileBitmap,
                                onPickImage = { profilePicker.launch(arrayOf("image/*")) },
                                onEditName = {
                                    editingName = profileName
                                    showNameDialog = true
                                }
                            )
                        }
                        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                        item { DrawerSectionLabel("ابزارهای برنامه") }
                        item { DrawerItem("خانه", Icons.Default.Home, screen == Screen.HOME) { navigateTo(Screen.HOME, true) } }
                        item { DrawerItem("استودیوی QR", Icons.Default.QrCode, screen == Screen.QR) { navigateTo(Screen.QR) } }
                        item { DrawerItem("ساخت Barcode", Icons.Default.TableRows, screen == Screen.BARCODE) { navigateTo(Screen.BARCODE) } }
                        item { DrawerItem("اسکنر", Icons.Default.QrCodeScanner, screen == Screen.SCANNER) { navigateTo(Screen.SCANNER) } }
                        item { DrawerItem("ساخت گروهی", Icons.Default.FileDownload, screen == Screen.BATCH) { navigateTo(Screen.BATCH) } }
                        item { DrawerItem("تاریخچه", Icons.Default.History, screen == Screen.HISTORY) { navigateTo(Screen.HISTORY) } }
                        item { DrawerItem("اشتراک حرفه‌ای", Icons.Default.Paid, screen == Screen.PREMIUM) { navigateTo(Screen.PREMIUM) } }
                        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                        item { DrawerItem("تنظیمات", Icons.Default.Settings, screen == Screen.SETTINGS) { navigateTo(Screen.SETTINGS) } }
                        item {
                            DrawerItem("معرفی به دوستان", Icons.Default.Share, false) {
                                scope.launch { drawerState.close() }
                                shareApp(activity)
                            }
                        }
                        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                        item { DrawerSectionLabel("ارتباط با ما") }
                        item { DrawerItem("درباره ما", Icons.Default.Info, screen == Screen.ABOUT_US) { navigateTo(Screen.ABOUT_US) } }
                        item { DrawerItem("تماس با ما", Icons.Default.ContactSupport, screen == Screen.CONTACT) { navigateTo(Screen.CONTACT) } }
                        item { DrawerItem("درباره نرم افزار", Icons.Default.QrCode, screen == Screen.ABOUT_APP) { navigateTo(Screen.ABOUT_APP) } }
                    }
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
                        Screen.HOME -> HomeScreen(onOpen = { navigateTo(it) })
                        Screen.QR -> QrMakerScreen(
                            isPremium = premiumState.active,
                            preferences = preferences,
                            onNeedPremium = { navigateTo(Screen.PREMIUM) },
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.BARCODE -> BarcodeMakerScreen(
                            isPremium = premiumState.active,
                            preferences = preferences,
                            onNeedPremium = { navigateTo(Screen.PREMIUM) },
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.SCANNER -> ScannerScreen(
                            preferences = preferences,
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.BATCH -> BatchToolsScreen(
                            isPremium = premiumState.active,
                            preferences = preferences,
                            onNeedPremium = { navigateTo(Screen.PREMIUM) },
                            onMessage = { scope.launch { snackbar.showSnackbar(it) } }
                        )
                        Screen.TEMPLATES -> TemplatesScreen(
                            isPremium = premiumState.active,
                            onOpenMaker = { navigateTo(Screen.QR) },
                            onNeedPremium = { navigateTo(Screen.PREMIUM) }
                        )
                        Screen.HISTORY -> HistoryScreen(preferences)
                        Screen.PREMIUM -> PremiumScreen(activity, billingManager, premiumState)
                        Screen.SETTINGS -> SettingsScreen(
                            preferences = preferences,
                            onEditProfileName = {
                                editingName = profileName
                                showNameDialog = true
                            },
                            onPickProfileImage = { profilePicker.launch(arrayOf("image/*")) }
                        )
                        Screen.ABOUT_US -> AboutUsScreen()
                        Screen.CONTACT -> ContactScreen()
                        Screen.ABOUT_APP -> AboutAppScreen()
                    }
                }
            }
        }
    }
}

// -------------------- صفحه خانه --------------------
@Composable
private fun HomeScreen(onOpen: (Screen) -> Unit) {
    val actions = remember {
        listOf(
            HomeAction("استودیوی QR", "لوگو، گرادیان، قاب و استایل", "🧩", Screen.QR),
            HomeAction("ساخت Barcode", "Code 128، EAN، PDF417...", "🏷️", Screen.BARCODE),
            HomeAction("اسکن کد", "دوربین، فلش و اسکن از عکس", "🔎", Screen.SCANNER),
            HomeAction("ساخت گروهی", "CSV / XLSX و صفحه لیبل A4", "📚", Screen.BATCH),
            HomeAction("طرح‌های آماده", "قالب‌های کاربردی و فانتزی", "🎀", Screen.TEMPLATES),
            HomeAction("تاریخچه", "جستجو، فیلتر و علاقه‌مندی", "🕘", Screen.HISTORY),
            HomeAction("حرفه‌ای", "خروجی HD، PDF و SVG", "👑", Screen.PREMIUM)
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeroCard() }
        item {
            Text("چی می‌خوای بسازی؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(620.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(actions) { action -> HomeActionCard(action) { onOpen(action.screen) } }
            }
        }
        item { CuteTip() }
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
                Text("QR یار • استودیوی کد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("بساز، شخصی‌سازی کن، گروهی خروجی بگیر و امن‌تر اسکن کن.", style = MaterialTheme.typography.bodyMedium)
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
            Text("برای QRهای لوگودار یا رنگی، امتیاز خوانایی را چک کن و قبل از چاپ یک اسکن واقعی بگیر.")
        }
    }
}

// -------------------- QR Studio --------------------
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
    var design by remember { mutableStateOf(QrDesignState()) }
    val undoStack = remember { mutableStateListOf<QrDesignState>() }
    val redoStack = remember { mutableStateListOf<QrDesignState>() }
    val presetStore = remember(context) { V20DesignPresetStore(context) }
    var presetName by remember { mutableStateOf("") }
    var presetRevision by remember { mutableStateOf(0) }
    val presets = remember(presetRevision) { presetStore.all() }

    fun commit(next: QrDesignState) {
        if (next == design) return
        undoStack.add(design)
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        design = next
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(design)
        design = undoStack.removeAt(undoStack.lastIndex)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(design)
        design = redoStack.removeAt(redoStack.lastIndex)
    }

    fun savePreset() {
        presetStore.save(V20DesignPreset(
            name = presetName.ifBlank { "طرح ${presets.size + 1}" },
            moduleStyle = design.moduleStyle, finderStyle = design.finderStyle,
            foreground = design.foreground, finderForeground = design.finderForeground,
            gradientEnd = design.gradientEnd, gradientEnabled = design.gradientEnabled,
            gradientDirection = design.gradientDirection, moduleScale = design.moduleScale,
            background = design.background, transparentBackground = design.transparentBackground,
            backgroundImageUri = design.backgroundImageUri, frameStyle = design.frameStyle,
            frameText = design.frameText, logoUri = design.logoUri, logoShape = design.logoShape,
            logoBorderColor = design.logoBorderColor
        ))
        presetName = ""
        presetRevision++
        onMessage("Preset طراحی ذخیره شد.")
    }

    fun loadPreset(preset: V20DesignPreset) {
        commit(QrDesignState(
            moduleStyle = preset.moduleStyle, finderStyle = preset.finderStyle,
            foreground = preset.foreground, finderForeground = preset.finderForeground,
            gradientEnd = preset.gradientEnd, gradientEnabled = preset.gradientEnabled,
            gradientDirection = preset.gradientDirection, moduleScale = preset.moduleScale,
            background = preset.background, transparentBackground = preset.transparentBackground,
            backgroundImageUri = preset.backgroundImageUri, frameStyle = preset.frameStyle,
            frameText = preset.frameText, logoUri = preset.logoUri, logoShape = preset.logoShape,
            logoBorderColor = preset.logoBorderColor
        ))
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            commit(design.copy(logoUri = uri.toString()))
        }
    }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            commit(design.copy(backgroundImageUri = uri.toString(), transparentBackground = false))
        }
    }

    val logoBitmap = remember(design.logoUri) { loadLocalBitmap(context, design.logoUri, 768) }
    val backgroundBitmap = remember(design.backgroundImageUri) { loadLocalBitmap(context, design.backgroundImageUri, 1024) }
    val generatorDesign = remember(design, logoBitmap) {
        QrDesign(
            moduleStyle = design.moduleStyle,
            finderStyle = design.finderStyle,
            foreground = design.foreground,
            finderForeground = design.finderForeground,
            gradientEnd = design.gradientEnd,
            gradientEnabled = design.gradientEnabled,
            gradientDirection = design.gradientDirection,
            moduleScale = design.moduleScale,
            background = design.background,
            transparentBackground = design.transparentBackground,
            backgroundImage = backgroundBitmap,
            frameStyle = design.frameStyle,
            frameText = design.frameText,
            logo = logoBitmap,
            logoShape = design.logoShape,
            logoBorderColor = design.logoBorderColor
        )
    }
    val payload = remember(kind, input) { buildQrPayload(kind, input) }
    val generated = remember(payload, generatorDesign) {
        runCatching { CodeGenerator.qr(payload, design = generatorDesign) }
    }
    val readability = remember(generatorDesign) { CodeGenerator.readability(generatorDesign) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CodePreview(generated.getOrNull(), generated.exceptionOrNull()?.message) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = ::undo, enabled = undoStack.isNotEmpty(), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Undo, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("برگشت طراحی")
                }
                OutlinedButton(onClick = ::redo, enabled = redoStack.isNotEmpty(), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Redo, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("دوباره")
                }
            }
        }
        item {
            SectionTitle("Presetهای طراحی")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = presetName, onValueChange = { presetName = it.take(30) },
                    modifier = Modifier.weight(1f), singleLine = true, label = { Text("نام Preset") }
                )
                Button(onClick = ::savePreset) { Text("ذخیره") }
            }
            if (presets.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { preset ->
                        OutlinedButton(onClick = { loadPreset(preset) }) { Text(preset.name) }
                    }
                }
            }
        }
        item {
            SectionTitle("نوع QR")
            ChipRow {
                QrKind.entries.forEach { item ->
                    ChoiceChip("${item.emoji} ${item.title}", selected = kind == item) {
                        kind = item
                        input = if (item == QrKind.URL || item == QrKind.SOCIAL) "https://" else ""
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
                placeholder = { Text(kind.hint) },
                supportingText = { Text("قالب ورودی: ${kind.hint}") }
            )
        }
        item {
            SectionTitle("استایل ماژول‌ها")
            ChipRow {
                ModuleStyle.entries.forEach { item ->
                    ChoiceChip(
                        label = item.title + if (item.premium) " 👑" else "",
                        selected = design.moduleStyle == item
                    ) { commit(design.copy(moduleStyle = item)) }
                }
            }
        }
        item {
            SectionTitle("استایل گوشه‌های Finder")
            ChipRow {
                FinderStyle.entries.forEach { item ->
                    ChoiceChip(
                        label = item.title + if (item.premium) " 👑" else "",
                        selected = design.finderStyle == item
                    ) { commit(design.copy(finderStyle = item)) }
                }
            }
        }
        item {
            SectionTitle("رنگ Finder مستقل 👑")
            ColorPalette(selected = design.finderForeground) { commit(design.copy(finderForeground = it)) }
            Spacer(Modifier.height(12.dp))
            Text("ضخامت ماژول", fontWeight = FontWeight.Bold)
            ChipRow {
                listOf(1f to "100%", 0.85f to "85%", 0.70f to "70%").forEach { (scale, title) ->
                    ChoiceChip(title, selected = design.moduleScale == scale) { commit(design.copy(moduleScale = scale)) }
                }
            }
        }
        item {
            SectionTitle("رنگ و گرادیان")
            SettingsInlineSwitch(
                title = "گرادیان",
                subtitle = "ترکیب دو رنگ روی کد",
                checked = design.gradientEnabled,
                onCheckedChange = { commit(design.copy(gradientEnabled = it)) }
            )
            Spacer(Modifier.height(10.dp))
            Text("رنگ اصلی", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ColorPalette(selected = design.foreground) { commit(design.copy(foreground = it)) }
            if (design.gradientEnabled) {
                Spacer(Modifier.height(12.dp))
                Text("رنگ انتهای گرادیان", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ColorPalette(selected = design.gradientEnd) { commit(design.copy(gradientEnd = it)) }
                Spacer(Modifier.height(12.dp))
                Text("جهت گرادیان", fontWeight = FontWeight.Bold)
                ChipRow {
                    GradientDirection.entries.forEach { direction ->
                        ChoiceChip(direction.title, selected = design.gradientDirection == direction) {
                            commit(design.copy(gradientDirection = direction))
                        }
                    }
                }
            }
        }
        item {
            SectionTitle("پس‌زمینه")
            SettingsInlineSwitch(
                title = "پس‌زمینه شفاف 👑",
                subtitle = "برای PNG مناسب است؛ روی چاپ سفید تست شود",
                checked = design.transparentBackground,
                onCheckedChange = { commit(design.copy(transparentBackground = it)) }
            )
            if (!design.transparentBackground) {
                Spacer(Modifier.height(10.dp))
                BackgroundPalette(selected = design.background) { commit(design.copy(background = it)) }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { backgroundPicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (design.backgroundImageUri == null) "تصویر پس‌زمینه 👑" else "تعویض تصویر")
                    }
                    if (design.backgroundImageUri != null) {
                        OutlinedButton(onClick = { commit(design.copy(backgroundImageUri = null)) }) { Text("حذف") }
                    }
                }
            }
        }
        item {
            SectionTitle("لوگو وسط QR 👑")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { logoPicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (design.logoUri == null) "انتخاب لوگو" else "تعویض لوگو")
                }
                if (design.logoUri != null) {
                    OutlinedButton(onClick = { commit(design.copy(logoUri = null)) }) { Text("حذف") }
                }
            }
            if (design.logoUri != null) {
                Spacer(Modifier.height(10.dp))
                Text("شکل لوگو", fontWeight = FontWeight.Bold)
                ChipRow {
                    LogoShape.entries.forEach { shape ->
                        ChoiceChip(shape.title, selected = design.logoShape == shape) { commit(design.copy(logoShape = shape)) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("رنگ حاشیه لوگو", fontWeight = FontWeight.Bold)
                ColorPalette(selected = design.logoBorderColor) { commit(design.copy(logoBorderColor = it)) }
            }
        }
        item {
            SectionTitle("قاب خروجی")
            ChipRow {
                FrameStyle.entries.forEach { item ->
                    ChoiceChip(
                        label = item.title + if (item.premium) " 👑" else "",
                        selected = design.frameStyle == item
                    ) { commit(design.copy(frameStyle = item)) }
                }
            }
            if (design.frameStyle == FrameStyle.LABEL) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = design.frameText,
                    onValueChange = { commit(design.copy(frameText = it.take(42))) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("متن زیر QR") }
                )
            }
        }
        item {
            ReadabilityCard(readability.score, readability.contrastRatio, readability.message, readability.good)
        }
        item {
            ExportPanel(
                professionalDesign = design.professional(),
                isPremium = isPremium,
                onFreePng = {
                    generated.getOrNull()?.let {
                        if (design.professional() && !isPremium) onNeedPremium()
                        else {
                            ExportManager.savePng(context, it.bitmap, "qr_${System.currentTimeMillis()}")
                            preferences.addHistory("QR", input)
                            onMessage("PNG ذخیره شد 🌸")
                        }
                    }
                },
                onPremiumPng = {
                    if (!isPremium) onNeedPremium() else runCatching {
                        val hd = CodeGenerator.qr(payload, size = 2048, design = generatorDesign)
                        ExportManager.savePng(context, hd.bitmap, "qr_hd_${System.currentTimeMillis()}")
                        preferences.addHistory("QR-HD", input)
                    }.onSuccess { onMessage("PNG با کیفیت HD ذخیره شد ✨") }
                        .onFailure { onMessage(it.message ?: "خطا در خروجی") }
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
                        onMessage(
                            if (it.hasLogo || it.frameStyle != FrameStyle.NONE || it.gradientEnd != null)
                                "SVG استاندارد ذخیره شد؛ لوگو/قاب/گرادیان در PNG و PDF حفظ می‌شوند."
                            else "SVG ذخیره شد"
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ColorPalette(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            AndroidColor.rgb(42, 34, 55),
            AndroidColor.rgb(74, 84, 130),
            AndroidColor.rgb(116, 71, 111),
            AndroidColor.rgb(55, 112, 98),
            AndroidColor.rgb(20, 82, 140),
            AndroidColor.rgb(160, 72, 64),
            AndroidColor.BLACK
        ).forEach { color -> ColorDot(color, selected == color) { onSelect(color) } }
    }
}

@Composable
private fun BackgroundPalette(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            AndroidColor.WHITE,
            AndroidColor.rgb(255, 248, 251),
            AndroidColor.rgb(245, 237, 247),
            AndroidColor.rgb(239, 249, 246)
        ).forEach { color -> ColorDot(color, selected == color) { onSelect(color) } }
    }
}

@Composable
private fun ReadabilityCard(score: Int, ratio: Double, message: String, good: Boolean) {
    val container = if (good) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    Card(colors = CardDefaults.cardColors(containerColor = container), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (good) Icons.Default.Verified else Icons.Default.WarningAmber, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("امتیاز خوانایی: $score/100", fontWeight = FontWeight.Black)
                Text("Contrast: ${String.format(Locale.US, "%.2f", ratio)}:1", style = MaterialTheme.typography.labelSmall)
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// -------------------- سازنده Barcode --------------------
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
    val generated = remember(input, selected) { runCatching { CodeGenerator.barcode(input, selected.second) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CodePreview(generated.getOrNull(), generated.exceptionOrNull()?.message, barcode = true) }
        item {
            SectionTitle("نوع بارکد")
            ChipRow { formats.forEach { item -> ChoiceChip(item.first, selected == item) { selected = item } } }
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
                    }.onSuccess { onMessage("PNG با کیفیت چاپ ذخیره شد") }
                        .onFailure { onMessage(it.message ?: "خطا") }
                },
                onPdf = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.savePdf(context, it.bitmap, "barcode_${System.currentTimeMillis()}")
                        preferences.addHistory("${selected.first}-PDF", input)
                        onMessage("PDF ذخیره شد")
                    }
                },
                onSvg = {
                    if (!isPremium) onNeedPremium() else generated.getOrNull()?.let {
                        ExportManager.saveSvg(context, it.matrix, it.foreground, it.background, "barcode_${System.currentTimeMillis()}")
                        preferences.addHistory("${selected.first}-SVG", input)
                        onMessage("SVG ذخیره شد")
                    }
                }
            )
        }
    }
}

// -------------------- Scanner: دوربین + عکس --------------------
@Composable
private fun ScannerScreen(preferences: PreferencesRepository, onMessage: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<DecodedImageCode>>(emptyList()) }

    val cameraLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (!result.contents.isNullOrBlank()) {
            val item = DecodedImageCode(result.contents, result.formatName ?: "CAMERA")
            results = listOf(item)
            preferences.addHistory("SCAN-${item.format}", item.text)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val decoded = withContext(Dispatchers.IO) { ImageCodeDecoder.decodeAll(context, uri) }
                results = decoded
                decoded.forEach { preferences.addHistory("SCAN-${it.format}", it.text) }
                onMessage(
                    if (decoded.isEmpty()) "کدی داخل این تصویر پیدا نشد."
                    else "${NumberFormatter.groupInteger(decoded.size.toLong())} کد از تصویر خوانده شد."
                )
            }
        }
    }

    fun launchCamera(torch: Boolean) {
        cameraLauncher.launch(
            ScanOptions()
                .setPrompt(if (torch) "فلش روشن است؛ کد را داخل کادر بگیر" else "کد را داخل کادر بگیر ✨")
                .setBeepEnabled(true)
                .setTorchEnabled(torch)
                .setOrientationLocked(false)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Text("🔍", fontSize = 66.sp) }
        item { Text("اسکنر QR و Barcode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
        item {
            Text(
                "با دوربین، فلش یا یک عکس از گالری اسکن کن. از یک تصویر می‌توان چند کد را هم‌زمان پیدا کرد.",
                textAlign = TextAlign.Center
            )
        }
        item {
            Button(onClick = { launchCamera(false) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("اسکن با دوربین")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { launchCamera(true) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(Modifier.width(5.dp))
                    Text("با فلش")
                }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(5.dp))
                    Text("از عکس")
                }
            }
        }
        if (results.isNotEmpty()) {
            item {
                Text(
                    "نتیجه‌ها (${NumberFormatter.groupInteger(results.size.toLong())})",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Black
                )
            }
            items(results, key = { "${it.format}:${it.text}" }) { item ->
                ScanResultCard(context, item)
            }
        }
    }
}

@Composable
private fun ScanResultCard(context: Context, item: DecodedImageCode) {
    val safety = remember(item.text) { ScanSafetyAnalyzer.analyze(item.text) }
    val settings = remember(context) { V19SettingsRepository(context) }
    var confirmOpen by remember(item.text) { mutableStateOf(false) }

    if (confirmOpen) {
        AlertDialog(
            onDismissRequest = { confirmOpen = false },
            title = { Text(if (safety.level == LinkRiskLevel.CAUTION) "هشدار لینک" else "باز کردن لینک") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(safety.message)
                    Text(item.text, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { confirmOpen = false; openUrl(context, item.text) }) { Text("باز کردن") }
            },
            dismissButton = { TextButton(onClick = { confirmOpen = false }) { Text("لغو") } }
        )
    }

    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(item.format, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(item.text, fontWeight = FontWeight.Medium)
            if (safety.isUrl) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (safety.level == LinkRiskLevel.CAUTION)
                        MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (safety.level == LinkRiskLevel.CAUTION) Icons.Default.WarningAmber else Icons.Default.Verified,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(safety.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { copyText(context, item.text) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("کپی")
                }
                OutlinedButton(onClick = { shareText(context, item.text) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("اشتراک")
                }
                if (safety.isUrl) {
                    OutlinedButton(
                        onClick = {
                            if (settings.confirmBeforeOpeningLinks) confirmOpen = true else openUrl(context, item.text)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("بازکردن")
                    }
                }
            }
        }
    }
}

// -------------------- Batch Tools --------------------
@Composable
private fun BatchToolsScreen(
    isPremium: Boolean,
    preferences: PreferencesRepository,
    onNeedPremium: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var table by remember { mutableStateOf<BatchTable?>(null) }
    var selectedColumn by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var paper by remember { mutableStateOf("A4") }
    var columns by remember { mutableStateOf(3) }
    var rows by remember { mutableStateOf(5) }
    val values = table?.columnValues(selectedColumn).orEmpty()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            loading = true
            scope.launch {
                table = withContext(Dispatchers.IO) { BatchInputReader.readTable(context, uri) }
                selectedColumn = 0
                loading = false
                val count = table?.rows?.size ?: 0
                onMessage(if (count == 0) "داده قابل استفاده‌ای در فایل پیدا نشد." else "${NumberFormatter.groupInteger(count.toLong())} ردیف خوانده شد.")
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ساخت گروهی حرفه‌ای", fontWeight = FontWeight.Black)
                    Text("CSV/TXT/XLSX تا 500 ردیف، انتخاب ستون، PNG، ZIP و PDF لیبل A4/A5 با چیدمان قابل تنظیم.")
                }
            }
        }
        item {
            Button(
                onClick = { picker.launch(arrayOf("text/*", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth(), enabled = !loading
            ) {
                Icon(Icons.Default.TableRows, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (loading) "در حال خواندن..." else "انتخاب CSV / TXT / XLSX")
            }
        }

        val currentTable = table
        if (currentTable != null && currentTable.columns.isNotEmpty()) {
            item {
                Text("ستون Payload", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentTable.columns.forEachIndexed { index, title ->
                        OutlinedButton(onClick = { selectedColumn = index }) {
                            Text(if (selectedColumn == index) "✓ $title" else title)
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تنظیم چاپ", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { paper = "A4"; columns = 3; rows = 5 }) { Text(if (paper == "A4") "✓ A4" else "A4") }
                            OutlinedButton(onClick = { paper = "A5"; columns = 2; rows = 4 }) { Text(if (paper == "A5") "✓ A5" else "A5") }
                            OutlinedButton(onClick = { columns = if (columns >= 6) 1 else columns + 1 }) { Text("ستون: $columns") }
                            OutlinedButton(onClick = { rows = if (rows >= 10) 1 else rows + 1 }) { Text("ردیف: $rows") }
                        }
                        Text("چیدمان سفارشی: $columns × $rows", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (values.isNotEmpty()) {
            item { Text("پیش‌نمایش ${NumberFormatter.groupInteger(values.size.toLong())} مقدار", fontWeight = FontWeight.Black) }
            items(values.take(10)) { value ->
                Card(shape = RoundedCornerShape(16.dp)) { Text(value, Modifier.fillMaxWidth().padding(12.dp), maxLines = 2) }
            }
            if (values.size > 10) item { Text("… و ${values.size - 10} مقدار دیگر", style = MaterialTheme.typography.bodySmall) }
            item {
                Button(
                    onClick = {
                        if (!isPremium) onNeedPremium() else scope.launch {
                            val count = withContext(Dispatchers.IO) {
                                values.forEachIndexed { index, value ->
                                    val code = CodeGenerator.qr(value, size = 768)
                                    try { ExportManager.savePng(context, code.bitmap, "batch_qr_${index + 1}_${System.currentTimeMillis()}") } finally { code.bitmap.recycle() }
                                }
                                values.size
                            }
                            preferences.addHistory("BATCH-PNG", "$count QR")
                            onMessage("${NumberFormatter.groupInteger(count.toLong())} فایل PNG ذخیره شد.")
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) { Text("خروجی PNG گروهی 👑") }
            }
            item {
                OutlinedButton(
                    onClick = {
                        if (!isPremium) onNeedPremium() else scope.launch {
                            runCatching { withContext(Dispatchers.IO) { ExportManager.saveQrZip(context, values, "qr_batch_${System.currentTimeMillis()}") } }
                                .onSuccess { preferences.addHistory("BATCH-ZIP", "${values.size} QR"); onMessage("ZIP گروهی در Downloads/QRStudio ذخیره شد.") }
                                .onFailure { onMessage(it.message ?: "ساخت ZIP ناموفق بود.") }
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) { Text("ساخت ZIP گروهی 👑") }
            }
            item {
                OutlinedButton(
                    onClick = {
                        if (!isPremium) onNeedPremium() else scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    val bitmaps = values.take(300).map { CodeGenerator.qr(it, size = 360).bitmap }
                                    try {
                                        val spec = if (paper == "A5") LabelPdfSpec.a5(columns, rows) else LabelPdfSpec.a4(columns, rows)
                                        ExportManager.saveLabelPdf(context, bitmaps, "qr_labels_${System.currentTimeMillis()}", spec)
                                    } finally { bitmaps.forEach { it.recycle() } }
                                }
                            }.onSuccess {
                                preferences.addHistory("BATCH-$paper", "${values.take(300).size} QR")
                                onMessage("PDF لیبل $paper با چیدمان $columns × $rows ذخیره شد.")
                            }.onFailure { onMessage(it.message ?: "ساخت PDF ناموفق بود.") }
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) { Text("ساخت PDF لیبل $paper 👑") }
            }
        }
    }
}

// -------------------- قالب‌های آماده --------------------
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

// -------------------- تاریخچه محلی --------------------
@Composable
private fun HistoryScreen(preferences: PreferencesRepository) {
    var revision by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var kindFilter by remember { mutableStateOf("همه") }
    var showClearDialog by remember { mutableStateOf(false) }
    val history = remember(revision) { preferences.history() }
    val filtered = remember(history, query, favoritesOnly, kindFilter) {
        history.filter { item ->
            val matchesQuery = query.isBlank() || item.payload.contains(query, true) || item.kind.contains(query, true)
            val matchesFavorite = !favoritesOnly || item.favorite
            val matchesKind = when (kindFilter) {
                "QR" -> item.kind.startsWith("QR") || item.kind.startsWith("BATCH")
                "Barcode" -> !item.kind.startsWith("QR") && !item.kind.startsWith("SCAN") && !item.kind.startsWith("BATCH")
                "Scan" -> item.kind.startsWith("SCAN")
                else -> true
            }
            matchesQuery && matchesFavorite && matchesKind
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("پاک‌کردن تاریخچه") },
            text = { Text("تمام رکوردهای تاریخچه و علاقه‌مندی‌ها حذف شوند؟") },
            confirmButton = {
                Button(onClick = {
                    preferences.clearHistory()
                    revision++
                    showClearDialog = false
                }) { Text("حذف همه") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("لغو") } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("جستجو در تاریخچه") }
            )
        }
        item {
            ChipRow {
                listOf("همه", "QR", "Barcode", "Scan").forEach { filter ->
                    ChoiceChip(filter, selected = kindFilter == filter) { kindFilter = filter }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { favoritesOnly = !favoritesOnly }, modifier = Modifier.weight(1f)) {
                    Icon(if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("فقط علاقه‌مندی‌ها")
                }
                OutlinedButton(onClick = { showClearDialog = true }, enabled = history.isNotEmpty()) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("پاک‌کردن")
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🫧", fontSize = 60.sp)
                    Text(if (history.isEmpty()) "هنوز چیزی اینجا نیست" else "نتیجه‌ای با این فیلتر پیدا نشد")
                }
            }
        } else {
            items(filtered, key = { it.createdAt }) { item ->
                Card(shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.kind, fontWeight = FontWeight.Bold)
                            Text(item.payload, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            Text(formatTime(item.createdAt), style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = {
                            preferences.toggleFavorite(item.createdAt)
                            revision++
                        }) {
                            Icon(
                                if (item.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "علاقه‌مندی"
                            )
                        }
                        IconButton(onClick = {
                            preferences.removeHistory(item.createdAt)
                            revision++
                        }) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
                    }
                }
            }
        }
    }
}

// -------------------- اشتراک حرفه‌ای --------------------
@Composable
private fun PremiumScreen(activity: Activity, billingManager: BillingManager, premiumState: PremiumState) {
    val formattedPrice = remember(premiumState.priceText) { NumberFormatter.groupNumbersInText(premiumState.priceText) }
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
                "ساخت و اسکن پایه رایگان می‌ماند. ابزارهای استودیویی و خروجی‌های چاپی با Pro فعال می‌شوند.",
                textAlign = TextAlign.Center
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumFeature("PNG با کیفیت چاپ و HD")
                PremiumFeature("PDF و SVG برداری")
                PremiumFeature("گرادیان، Finder، لوگو، قاب و پس‌زمینه شفاف")
                PremiumFeature("ساخت گروهی از CSV/XLSX و صفحه لیبل A4")
                PremiumFeature("قالب‌های حرفه‌ای و خروجی نامحدود در مدت اشتراک")
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
                    Text(if (premiumState.available) "فعال‌سازی $formattedPrice" else "در انتظار تنظیم محصول فروشگاه")
                }
            }
        }
        premiumState.message?.let { item { Text(it, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary) } }
    }
}

// -------------------- تنظیمات --------------------
@Composable
private fun SettingsScreen(
    preferences: PreferencesRepository,
    onEditProfileName: () -> Unit,
    onPickProfileImage: () -> Unit
) {
    var notifications by remember { mutableStateOf(preferences.notificationsEnabled) }
    val historyCount = remember { preferences.history().size }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsRow("نام پروفایل", preferences.profileName, Icons.Default.AccountCircle) {
                IconButton(onClick = onEditProfileName) { Icon(Icons.Default.Edit, contentDescription = "ویرایش نام") }
            }
        }
        item {
            SettingsRow("عکس پروفایل", "تصویر بالای منوی همبرگری", Icons.Default.AddPhotoAlternate) {
                TextButton(onClick = onPickProfileImage) { Text("انتخاب") }
            }
        }
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
        item { SettingsRow("تاریخچه محلی", "${NumberFormatter.groupInteger(historyCount.toLong())} رکورد", Icons.Default.History) { Text("آفلاین") } }
        item { SettingsRow("نسخه برنامه", "Version ${BuildConfig.VERSION_NAME}", Icons.Default.Verified) { Text("${BuildConfig.VERSION_CODE}") } }
    }
}

@Composable
private fun AboutUsScreen() = CenterInfo {
    Text("گروه توسعه و برنامه نویسی AS Team", fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text("تمامی حقوق مربوط به این برنامه انحصاری میباشد", textAlign = TextAlign.Center)
}

// تماس با ما: اطلاعات اصلی بالا و امضای AS Team در بخش پایین صفحه پس از Divider قرار می‌گیرد.
@Composable
private fun ContactScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(26.dp))
        Icon(Icons.Default.ContactSupport, contentDescription = null, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(14.dp))
        Text("ارتباط با پشتیبانی", fontWeight = FontWeight.Black, fontSize = 22.sp)
        Spacer(Modifier.height(18.dp))
        Text("ایمیل پشتیبانی", fontWeight = FontWeight.Bold)
        Text("as.team.support@gmail.com")
        Spacer(Modifier.weight(1f))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        Text("گروه توسعه فناوری و نرم افزاری as Team", fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("as.team.support@gmail.com", textAlign = TextAlign.Center)
        Spacer(Modifier.height(44.dp))
    }
}

// صفحه درباره نرم‌افزار فقط توضیح کاربرپسند و نسخه را نمایش می‌دهد؛ شناسه‌های فنی نمایش داده نمی‌شوند.
@Composable
private fun AboutAppScreen() = CenterInfo {
    Text("QR ساز و Barcode ساز", fontWeight = FontWeight.Black, fontSize = 22.sp)
    Spacer(Modifier.height(12.dp))
    Text(
        "ابزار فارسی برای ساخت و اسکن QR Code و انواع Barcode.\n" +
            "در استودیوی QR می‌توانید رنگ، گرادیان، لوگو، قاب و گوشه‌های کد را شخصی‌سازی کنید.\n" +
            "اسکن از دوربین و تصویر، تاریخچه قابل جستجو و ساخت گروهی فایل‌های QR نیز در دسترس است.",
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Text("نسخه ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Bold)
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

// -------------------- Drawer --------------------
@Composable
private fun DrawerProfileHeader(
    premium: Boolean,
    profileName: String,
    profileBitmap: Bitmap?,
    onPickImage: () -> Unit,
    onEditName: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap.asImageBitmap(),
                    contentDescription = "عکس پروفایل",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(66.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(profileName, fontWeight = FontWeight.Black)
            IconButton(onClick = onEditName, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "ویرایش نام", modifier = Modifier.size(18.dp))
            }
        }
        Text(
            if (premium) "حالت حرفه‌ای فعال 👑" else "QR یار • کدهای کوچک، خروجی‌های جدی ✨",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Text("برای تغییر عکس، روی تصویر بزنید", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
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

// -------------------- اجزای مشترک UI --------------------
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
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
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
                if (professionalDesign && !isPremium)
                    "این طراحی حرفه‌ای است؛ برای خروجی نهایی اشتراک Pro لازم است."
                else "PNG استاندارد رایگان است؛ فرمت‌های چاپی و HD حرفه‌ای هستند.",
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
        Text(
            label,
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
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

@Composable
private fun SettingsInlineSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// -------------------- توابع کمکی --------------------
private fun buildQrPayload(kind: QrKind, input: String): String {
    if (input.isBlank()) return ""
    return when (kind) {
        QrKind.URL, QrKind.TEXT, QrKind.SOCIAL -> input.trim()
        QrKind.EMAIL -> "mailto:${input.trim()}"
        QrKind.PHONE -> "tel:${input.trim()}"
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
        QrKind.VCARD -> {
            val parts = input.split("|", limit = 5)
            val name = escapeVcard(parts.getOrElse(0) { "" })
            val phone = escapeVcard(parts.getOrElse(1) { "" })
            val email = escapeVcard(parts.getOrElse(2) { "" })
            val org = escapeVcard(parts.getOrElse(3) { "" })
            val url = escapeVcard(parts.getOrElse(4) { "" })
            buildString {
                append("BEGIN:VCARD\nVERSION:3.0\nFN:$name\n")
                if (phone.isNotBlank()) append("TEL:$phone\n")
                if (email.isNotBlank()) append("EMAIL:$email\n")
                if (org.isNotBlank()) append("ORG:$org\n")
                if (url.isNotBlank()) append("URL:$url\n")
                append("END:VCARD")
            }
        }
        QrKind.EVENT -> {
            val parts = input.split("|", limit = 4)
            val title = parts.getOrElse(0) { "" }
            val start = parts.getOrElse(1) { "" }
            val end = parts.getOrElse(2) { "" }
            val location = parts.getOrElse(3) { "" }
            "BEGIN:VEVENT\nSUMMARY:$title\nDTSTART:$start\nDTEND:$end\nLOCATION:$location\nEND:VEVENT"
        }
        QrKind.GEO -> "geo:${input.trim()}"
    }
}

private fun escapeWifi(value: String): String = value
    .replace("\\", "\\\\")
    .replace(";", "\\;")
    .replace(",", "\\,")
    .replace(":", "\\:")

private fun escapeVcard(value: String): String = value
    .replace("\\", "\\\\")
    .replace(";", "\\;")
    .replace(",", "\\,")
    .replace("\n", "\\n")

private fun shareApp(activity: Activity) {
    shareText(activity, "QR ساز و Barcode ساز AS Team\nhttps://github.com/waxew/App-QrCodeYar", "معرفی به دوستان")
}

private fun shareText(context: Context, text: String, chooserTitle: String = "اشتراک‌گذاری") {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            chooserTitle
        )
    )
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("QR result", text))
}

private fun openUrl(context: Context, text: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text)))
    }
}

// تصویر انتخاب‌شده را با حداکثر ضلع مشخص بارگذاری می‌کند تا حافظه UI کنترل شود.
private fun loadLocalBitmap(context: Context, uriString: String?, maxSide: Int): Bitmap? {
    if (uriString.isNullOrBlank()) return null
    val original = runCatching {
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use { BitmapFactory.decodeStream(it) }
    }.getOrNull() ?: return null
    val largest = maxOf(original.width, original.height)
    if (largest <= maxSide) return original
    val ratio = maxSide.toFloat() / largest.toFloat()
    val width = (original.width * ratio).toInt().coerceAtLeast(1)
    val height = (original.height * ratio).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(original, width, height, true)
    if (scaled !== original) original.recycle()
    return scaled
}

private fun formatTime(time: Long): String = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(time))

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateCompat(): androidx.compose.runtime.State<T> = collectAsState()
