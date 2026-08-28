# App-QrCodeYar

اپلیکیشن اندرویدی فارسی برای ساخت، شخصی‌سازی، اسکن و خروجی گرفتن از QR Code و Barcode با رابط RTL و طراحی فانتزی Material 3.

## نسخه فعلی

- Version: **1.9.0**
- Version code: **9**
- Application ID: `com.waxew.qrbarcode`
- Minimum Android: API 23
- Target Android: API 35
- Kotlin + Jetpack Compose

## امکانات اصلی نسخه 1.9.0

- QR Studio با استایل کلاسیک، گرد، نقطه‌ای و حبابی، Finder مستقل، گرادیان، لوگو، قاب، پس‌زمینه شفاف و Undo/Redo
- امتیاز Readability/Contrast برای کاهش QRهای سخت‌اسکن
- QR برای URL، متن، Wi-Fi، ایمیل، تلفن، SMS، vCard، Event، Geo و Social
- Barcodeهای Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- Barcode/Product Label Studio برای پیش‌نمایش لیبل فروشگاهی شامل نام محصول، قیمت و کد کالا
- Smart Template Catalog برای Wi-Fi، کارت ویزیت، رستوران، شبکه اجتماعی، محصول و موقعیت
- Scanner زنده CameraX + ML Kit با Torch، Zoom و Multi-code
- اسکن از Gallery و تشخیص چند کد در یک تصویر
- تنظیمات نسخه 1.9 برای Beep، لرزش، اسکن متوالی، جلوگیری از نتیجه تکراری و تأیید پیش از بازکردن لینک
- تحلیل امنیتی آفلاین URL شامل HTTP، IP مستقیم، Punycode، @ و لینک‌های کوتاه‌شده
- تاریخچه محلی Room با Search، Filter، Favorite و Delete
- ساخت گروهی QR از CSV/TXT/XLSX، خروجی PNG گروهی و PDF لیبل A4
- بکاپ JSON محلی از تاریخچه و تنظیمات نسخه 1.9
- مدل Archive Folder و Tag برای توسعه آرشیو ساختاریافته
- تنظیمات شخصی‌سازی Compact Mode، Accent و Start Page در Repository نسخه 1.9
- قفل برنامه با PIN چهار تا هشت رقمی؛ فقط SHA-256 PIN ذخیره می‌شود
- Drawer راست‌چین، پروفایل، تنظیم اعلان‌ها، Dark Mode خودکار و Back stack داخلی
- خروجی PNG، PNG HD، PDF و SVG
- بررسی نسخه جدید از `distribution/latest.json`
- مدل Freemium و اشتراک هفتگی Pro
- قالب‌بندی سه‌رقمی قیمت‌ها؛ `12000000` → `12,000,000`

## فایل‌های مهم نسخه 1.9

- `app/src/main/java/com/waxew/qrbarcode/ui/QrBarcodeApp.kt` — رابط اصلی و قابلیت‌های نسخه 1.1
- `app/src/main/java/com/waxew/qrbarcode/ui/V19Root.kt` — مرکز قابلیت‌های جدید 1.9 و اتصال به رابط اصلی
- `app/src/main/java/com/waxew/qrbarcode/v19/V19SettingsRepository.kt` — تنظیمات اسکنر و شخصی‌سازی
- `app/src/main/java/com/waxew/qrbarcode/v19/V19Toolbox.kt` — قالب‌ها، امنیت URL، لیبل فروشگاهی و بکاپ
- `app/src/main/java/com/waxew/qrbarcode/v19/V19AppLock.kt` — قفل PIN محلی
- `generator/CodeGenerator.kt` — موتور QR/Barcode
- `scanner/ModernScannerActivity.kt` — CameraX + ML Kit
- `data/HistoryDatabase.kt` — Room History
- `batch/BatchInputReader.kt` — CSV/TXT/XLSX
- `export/ExportManager.kt` — PNG/PDF/SVG و A4 Label PDF
- `billing/BillingManager.kt` — اشتراک Pro
- `update/UpdateChecker.kt` — Update checker

## وضعیت Build

نسخه 1.9.0 در GitHub Actions با Gradle 8.9، JDK 17 و Android SDK 35 برای هر دو خروجی Debug و Release کامپایل شده است.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## بروزرسانی و امضا

برای اینکه نسخه‌های بعدی روی نسخه نصب‌شده Update شوند، `applicationId` ثابت می‌ماند و Releaseهای Production باید با همان Release Key امضا شوند. کلید خصوصی Release در GitHub عمومی Commit نمی‌شود.

## حریم خصوصی

History، پروفایل، تنظیمات 1.9 و PIN hash به‌صورت محلی روی دستگاه نگه‌داری می‌شوند. تحلیل اولیه لینک نیز آفلاین است.

## قابلیت‌های نیازمند Backend

Dynamic QR واقعی، Cloud Sync، Analytics ابری و اعتبارسنجی سروری خرید هنوز Backend می‌خواهند و در 1.9.0 فعال تلقی نمی‌شوند.

## نکته توسعه

برخی گزینه‌های 1.9 مانند Folder/Tag، Accent/Start Page و تنظیمات پیشرفته Scanner زیرساخت ذخیره‌سازی و UI خود را دارند و برای یکپارچه‌سازی عمیق‌تر با تمام جریان‌های قدیمی در نسخه بعدی قابل توسعه هستند. این موارد در README به‌عنوان قابلیت Backend یا Sync معرفی نشده‌اند.
