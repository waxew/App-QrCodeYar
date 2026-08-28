# راهنمای سورس App-QrCodeYar v1.1.0

این راهنما مسئولیت فایل‌ها، مسیر تغییرات و قواعد انتشار را توضیح می‌دهد. داخل فایل‌های Kotlin/Gradle/XML نیز کامنت‌های توضیحی وجود دارد.

## فایل‌های ریشه

- `settings.gradle.kts`: نام پروژه، مخازن dependency و معرفی ماژول `app`.
- `build.gradle.kts`: نسخه پلاگین Android/Kotlin/Compose.
- `gradle.properties`: تنظیمات AndroidX و Gradle.
- `.gitignore`: جلوگیری از Commit فایل‌های Build، IDE و signing خصوصی.
- `distribution/latest.json`: اطلاعات آخرین نسخه برای Update Checker.
- `.github/workflows/android.yml`: Build مستقل Debug و unsigned Release در GitHub Actions.

## ماژول app

- `app/build.gradle.kts`: applicationId ثابت، versionCode/versionName، SDK، Build Typeها، Signing و dependencyها.
- `AndroidManifest.xml`: مجوز اینترنت/دوربین و تعریف `MainActivity`.
- `MainActivity.kt`: نقطه ورود، ساخت `BillingManager` و `PreferencesRepository` و راه‌اندازی Compose.

## UI و ناوبری

- `ui/QrBarcodeApp.kt`: خانه، QR Studio، Barcode، Scanner، Batch، Template، History، Premium، Settings، About و Drawer.
- تابع `navigateTo()` تنها مسیر استاندارد تغییر صفحه است.
- `BackHandler` ابتدا Drawer را می‌بندد، بعد Back Stack را Pop می‌کند و فقط در HOME خروج را به Android می‌سپارد.
- Drawer بالای خود پروفایل دایره‌ای دارد؛ عکس با Storage Access Framework انتخاب و URI آن محلی ذخیره می‌شود.

## موتور QR/Barcode

- `generator/CodeGenerator.kt`:
  - ZXing برای ساخت BitMatrix.
  - `QrDesign` برای استایل ماژول، Finder، گرادیان، پس‌زمینه، قاب و لوگو.
  - Error Correction QR روی H.
  - `readability()` برای تخمین Contrast و امتیاز خوانایی.
- Finderها مستقل از Module Style کنترل می‌شوند تا طراحی انعطاف‌پذیر باشد.

## Scanner

- دوربین همچنان از JourneyApps ZXing Embedded استفاده می‌کند.
- `scanner/ImageCodeDecoder.kt` عکس Gallery را با ZXing Core پردازش می‌کند.
- `GenericMultipleBarcodeReader` امکان چند کد در یک تصویر را می‌دهد.
- `ScanSafetyAnalyzer` فقط ساختار URL را آفلاین بررسی می‌کند و هیچ درخواست شبکه‌ای برای URL اسکن‌شده ارسال نمی‌کند.

## Batch

- `batch/BatchInputReader.kt`:
  - CSV/TXT: اولین ستون هر خط.
  - XLSX: Sheet اول و اولین سلول غیرخالی هر ردیف.
  - حداکثر 100 payload.
- `ExportManager.saveA4LabelPdf()` خروجی 3×5 لیبل در هر صفحه A4 تولید می‌کند.

## تاریخچه و پروفایل

- `data/PreferencesRepository.kt` تنظیمات، نام/URI پروفایل و تاریخچه را ذخیره می‌کند.
- رکوردهای قدیمی بدون فیلد `favorite` با مقدار false Migration می‌شوند.
- History جدید تا 100 رکورد نگه می‌دارد و Favorite/حذف را پشتیبانی می‌کند.

## قیمت

- `util/NumberFormatter.kt` ارقام فارسی/عربی را به لاتین تبدیل می‌کند و گروه‌های عددی بلند را سه‌رقمی جدا می‌کند.
- `BillingManager` قیمت فروشگاه را قبل از نمایش از این Formatter عبور می‌دهد.

## خروجی

- `export/ExportManager.kt`:
  - PNG در Pictures/QRStudio.
  - PDF/SVG در Download/QRStudio.
  - A4 Label PDF برای Batch.
- SVG از BitMatrix استاندارد ساخته می‌شود؛ افکت‌های Bitmap مثل Logo/Frame/Gradient در PNG/PDF کامل‌تر هستند.

## قانون انتشار نسخه بعدی

1. `applicationId = com.waxew.qrbarcode` تغییر نکند.
2. `versionCode` همیشه افزایش یابد.
3. `versionName` افزایش یابد.
4. Release با همان keystore خصوصی نسخه‌های قبل امضا شود.
5. `distribution/latest.json` به‌روزرسانی شود.
6. signing key، password و `info.txt` خصوصی هرگز در GitHub عمومی Commit نشوند.
7. قبل از انتشار Store یک تست نصب روی نسخه قبلی و یک تست نصب تمیز روی دستگاه واقعی انجام شود.

## قابلیت‌هایی که Backend لازم دارند

- Dynamic QR با Redirect قابل تغییر
- Analytics تعداد اسکن/کشور/دستگاه
- Cloud Sync
- Server-side purchase verification

این موارد باید بعد از انتخاب Backend (مثلاً Supabase/Cloudflare/سرور اختصاصی) در نسخه جدا توسعه داده شوند؛ بدون Backend واقعی نباید با داده ساختگی فعال شوند.
