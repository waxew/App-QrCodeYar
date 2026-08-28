# App-QrCodeYar

اپلیکیشن اندرویدی فارسی برای ساخت، شخصی‌سازی، اسکن و خروجی گرفتن از QR Code و Barcode با رابط RTL و طراحی فانتزی Material 3.

## نسخه فعلی

- Version: **1.9.1**
- Version code: **10**
- Application ID: `com.waxew.qrbarcode`
- Minimum Android: API 23
- Target Android: API 35
- Kotlin + Jetpack Compose

## امکانات اصلی نسخه 1.9.1

- QR Studio با استایل کلاسیک، گرد، نقطه‌ای و حبابی، Finder مستقل، گرادیان، لوگو، قاب، پس‌زمینه شفاف و Undo/Redo
- امتیاز Readability/Contrast برای کاهش QRهای سخت‌اسکن
- QR برای URL، متن، Wi-Fi، ایمیل، تلفن، SMS، vCard، Event، Geo و Social
- Barcodeهای Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- Barcode/Product Label Studio برای پیش‌نمایش لیبل فروشگاهی شامل نام محصول، قیمت و کد کالا
- Smart Template Catalog برای Wi-Fi، کارت ویزیت، رستوران، شبکه اجتماعی، محصول و موقعیت
- Scanner زنده CameraX + ML Kit با Torch، تشخیص چندکدی و اسکن از Gallery
- تنظیمات واقعی Scanner برای Beep، Vibrate، Continuous Scan و Prevent Duplicates
- تحلیل امنیتی آفلاین URL شامل HTTP، IP مستقیم، Punycode، @ و لینک‌های کوتاه‌شده
- تاریخچه محلی Room با Search، Filter، Favorite و Delete
- Folder و Tag واقعی برای هر رکورد تاریخچه با Migration امن دیتابیس از schema 1 به 2
- Archive Manager برای جستجو، فیلتر پوشه و ویرایش Folder/Tag
- ساخت گروهی QR از CSV/TXT/XLSX، خروجی PNG گروهی و PDF لیبل A4
- بکاپ JSON محلی از تاریخچه و تنظیمات نسخه 1.9
- Accent واقعی Material 3 با پالت صورتی یاسی، سبز نعنایی و آبی آسمانی
- Start Page عملی با انتخاب خانه، اسکنر یا مرکز 1.9
- Compact Mode و قفل برنامه با PIN چهار تا هشت رقمی؛ فقط SHA-256 PIN ذخیره می‌شود
- Drawer راست‌چین، پروفایل، تنظیم اعلان‌ها، Dark Mode خودکار و Back stack داخلی
- خروجی PNG، PNG HD، PDF و SVG
- بررسی نسخه جدید از `distribution/latest.json`
- مدل Freemium و اشتراک هفتگی Pro
- قالب‌بندی سه‌رقمی قیمت‌ها؛ `12000000` → `12,000,000`

## حفظ داده هنگام بروزرسانی

نسخه 1.9.1 دیتابیس History را از schema 1 به schema 2 با Migration صریح Room ارتقا می‌دهد. دو ستون Folder و Tags با مقدار اولیه خالی اضافه می‌شوند و رکوردهای قبلی، Favoriteها و زمان ثبت آن‌ها حذف نمی‌شوند. `applicationId` نیز ثابت مانده است.

## فایل‌های مهم

- `app/src/main/java/com/waxew/qrbarcode/ui/QrBarcodeApp.kt` — رابط اصلی برنامه
- `app/src/main/java/com/waxew/qrbarcode/ui/V19Root.kt` — مرکز قابلیت‌های 1.9، Archive Manager و Start Page
- `app/src/main/java/com/waxew/qrbarcode/v19/V19SettingsRepository.kt` — تنظیمات اسکنر و شخصی‌سازی
- `app/src/main/java/com/waxew/qrbarcode/v19/V19Toolbox.kt` — قالب‌ها، امنیت URL، لیبل فروشگاهی و بکاپ
- `app/src/main/java/com/waxew/qrbarcode/v19/V19AppLock.kt` — قفل PIN محلی
- `generator/CodeGenerator.kt` — موتور QR/Barcode
- `scanner/V19ScannerActivity.kt` — Scanner متصل به تنظیمات 1.9
- `scanner/ModernScannerActivity.kt` — Scanner نسل قبلی برای سازگاری سورس
- `data/HistoryDatabase.kt` — Room History + Folder/Tag + Migration
- `data/PreferencesRepository.kt` — Repository تاریخچه و متادیتای آرشیو
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

## بروزرسانی و امضا

برای اینکه نسخه‌های بعدی روی نسخه نصب‌شده Update شوند، `applicationId` ثابت می‌ماند و Releaseهای Production باید با همان Release Key امضا شوند. کلید خصوصی Release در GitHub عمومی Commit نمی‌شود.

## حریم خصوصی

History، Folder/Tag، پروفایل، تنظیمات 1.9 و PIN hash به‌صورت محلی روی دستگاه نگه‌داری می‌شوند. تحلیل اولیه لینک نیز آفلاین است.

## قابلیت‌های نیازمند Backend

Dynamic QR واقعی، Cloud Sync، Analytics ابری و اعتبارسنجی سروری خرید هنوز Backend می‌خواهند و در 1.9.1 فعال تلقی نمی‌شوند.
