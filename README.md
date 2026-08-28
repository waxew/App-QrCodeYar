# App-QrCodeYar

اپلیکیشن اندرویدی فارسی برای ساخت، شخصی‌سازی، اسکن، آرشیو و خروجی گرفتن از QR Code و Barcode با رابط RTL و طراحی Material 3.

## نسخه فعلی

- Version: **1.9.2**
- Version code: **11**
- Application ID: `com.waxew.qrbarcode`
- Minimum Android: API 23
- Target Android: API 35
- Kotlin + Jetpack Compose

## امکانات اصلی نسخه 1.9.2

- QR Studio با استایل کلاسیک، گرد، نقطه‌ای و حبابی، Finder مستقل، گرادیان، لوگو، قاب، پس‌زمینه شفاف و Undo/Redo
- امتیاز Readability/Contrast برای کاهش QRهای سخت‌اسکن
- QR برای URL، متن، Wi-Fi، ایمیل، تلفن، SMS، vCard، Event، Geo و Social
- Smart Template Payload Builder واقعی برای Wi-Fi، کارت ویزیت، رستوران، شبکه اجتماعی، محصول و موقعیت
- Barcodeهای Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- Barcode/Product Label Studio با نام محصول، قیمت، کد کالا و ابعاد خروجی قابل کنترل
- Scanner زنده CameraX + ML Kit با Torch، تشخیص چندکدی و اسکن از Gallery
- تنظیمات واقعی Scanner برای Beep، Vibrate، Continuous Scan و Prevent Duplicates
- تحلیل امنیتی آفلاین URL شامل HTTP، IP مستقیم، localhost، Punycode، @، لینک کوتاه و Schemeهای پرخطر
- تاریخچه محلی Room با Search، Filter، Favorite و Delete
- Folder و Tag واقعی برای هر رکورد تاریخچه با Migration امن دیتابیس از schema 1 به 2
- Archive Manager برای جستجو، فیلتر پوشه و ویرایش Folder/Tag
- ساخت گروهی QR از CSV/TXT/XLSX، خروجی PNG گروهی و PDF لیبل A4
- Backup schema 2 شامل تاریخچه، Favorite، Folder، Tag و تنظیمات
- Restore امن تنظیمات و History/Folder/Tag از فایل JSON؛ PIN از بکاپ خودکار Restore نمی‌شود
- امکان بازکردن فایل بکاپ JSON از File Manager با خود QR یار
- Accent واقعی Material 3 با پالت صورتی یاسی، سبز نعنایی و آبی آسمانی
- Start Page عملی با انتخاب خانه، اسکنر یا مرکز 1.9
- Compact Mode واقعی با کاهش کنترل‌شده Density رابط Compose
- قفل برنامه با PIN چهار تا هشت رقمی؛ فقط SHA-256 PIN ذخیره می‌شود
- Drawer راست‌چین، پروفایل، تنظیم اعلان‌ها، Dark Mode خودکار و Back stack داخلی
- خروجی PNG، PNG HD، PDF و SVG
- بررسی نسخه جدید از `distribution/latest.json`
- مدل Freemium و اشتراک هفتگی Pro
- قالب‌بندی سه‌رقمی قیمت‌ها؛ `12000000` → `12,000,000`

## حفظ داده هنگام بروزرسانی

Room Migration صریح، `applicationId` ثابت و Repository سازگار با نسخه‌های قبل باعث می‌شوند History، Favorite، Folder/Tag، تنظیمات و پروفایل هنگام نصب نسخه جدید حفظ شوند. Release Production باید همیشه با همان Release Key امضا شود.

## فایل‌های مهم

- `app/src/main/java/com/waxew/qrbarcode/ui/QrBarcodeApp.kt` — رابط اصلی برنامه
- `app/src/main/java/com/waxew/qrbarcode/ui/V19Root.kt` — مرکز قابلیت‌های 1.9، Archive Manager و Start Page
- `app/src/main/java/com/waxew/qrbarcode/v19/V19SettingsRepository.kt` — تنظیمات + Backup/Restore تنظیمات
- `app/src/main/java/com/waxew/qrbarcode/v19/V19Toolbox.kt` — Smart Templates، امنیت URL، لیبل و Backup parser
- `app/src/main/java/com/waxew/qrbarcode/backup/BackupRestoreActivity.kt` — Restore فایل JSON از File Manager
- `app/src/main/java/com/waxew/qrbarcode/v19/V19AppLock.kt` — قفل PIN محلی
- `generator/CodeGenerator.kt` — موتور QR/Barcode
- `scanner/V19ScannerActivity.kt` — Scanner متصل به تنظیمات 1.9
- `scanner/ModernScannerActivity.kt` — Scanner نسل قبلی برای سازگاری سورس
- `data/HistoryDatabase.kt` — Room History + Folder/Tag + Migration
- `data/PreferencesRepository.kt` — Repository تاریخچه، Archive و Restore Room
- `batch/BatchInputReader.kt` — CSV/TXT/XLSX
- `export/ExportManager.kt` — PNG/PDF/SVG و A4 Label PDF
- `billing/BillingManager.kt` — اشتراک Pro
- `update/UpdateChecker.kt` — Update checker

## Build

CI پروژه با Gradle 8.9، JDK 17 و Android SDK 35 هر دو خروجی Debug و Release را کامپایل می‌کند.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## حریم خصوصی

History، Folder/Tag، پروفایل، تنظیمات و PIN hash به‌صورت محلی روی دستگاه نگه‌داری می‌شوند. Link Security نیز آفلاین است.

## قابلیت‌های نیازمند Backend

Dynamic QR واقعی، Cloud Sync، Analytics ابری و اعتبارسنجی سروری خرید ذاتاً به Backend نیاز دارند و جزو بسته آفلاین 1.9.2 محسوب نمی‌شوند.
