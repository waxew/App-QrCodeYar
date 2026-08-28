# App-QrCodeYar

اپلیکیشن اندرویدی فارسی برای ساخت، شخصی‌سازی، اسکن و خروجی گرفتن از QR Code و Barcode با رابط RTL و طراحی فانتزی Material 3.

## نسخه فعلی

- Version: **1.1.0**
- Version code: **3**
- Application ID: `com.waxew.qrbarcode`
- Minimum Android: API 23
- Target Android: API 35
- Kotlin + Jetpack Compose

## امکانات نسخه 1.1.0

- QR Studio با استایل ماژول کلاسیک، گرد، نقطه‌ای و حبابی
- استایل مستقل Finderهای QR
- گرادیان دو رنگ، رنگ پس‌زمینه و PNG با پس‌زمینه شفاف
- قراردادن لوگو در مرکز QR با Error Correction سطح H
- قاب گرد و قاب متنی برای QR
- Undo / Redo تنظیمات طراحی
- امتیاز خوانایی و Contrast برای کاهش طراحی‌های سخت‌اسکن
- QR برای لینک، متن، Wi-Fi، ایمیل، تلفن، SMS، vCard، رویداد، موقعیت جغرافیایی و شبکه اجتماعی
- ساخت Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- اسکن QR/Barcode با دوربین و حالت فلش
- اسکن از تصویر گالری و پیدا کردن چند کد در یک تصویر
- هشدار آفلاین برای برخی نشانه‌های لینک مشکوک قبل از بازکردن URL
- ساخت گروهی QR از CSV/TXT/XLSX و خروجی PNG گروهی
- PDF لیبل A4 چندصفحه‌ای برای چاپ گروهی
- تاریخچه محلی با جستجو، فیلتر، Favorite، حذف تکی و پاک‌کردن همه
- Drawer راست‌چین با عکس پروفایل دایره‌ای، نام کاربر و ابزارهای اختصاصی برنامه
- تنظیم اعلان‌ها و Dark Mode خودکار
- Back stack داخلی؛ Back ابتدا به صفحه قبلی برمی‌گردد و فقط از HOME می‌تواند برنامه را ببندد
- خروجی PNG، PNG HD، PDF و SVG
- بررسی نسخه جدید از `distribution/latest.json`
- مدل Freemium و اشتراک هفتگی Pro
- قالب‌بندی سه‌رقمی قیمت‌ها؛ نمونه `12000000` → `12,000,000`

## ساختار مهم سورس

- `app/src/main/java/com/waxew/qrbarcode/ui/QrBarcodeApp.kt` — صفحه‌ها، Drawer، ناوبری و اتصال امکانات
- `generator/CodeGenerator.kt` — موتور QR/Barcode و QR Studio
- `scanner/ImageCodeDecoder.kt` — اسکن از عکس، چندکدی و تحلیل ایمنی URL
- `batch/BatchInputReader.kt` — خواندن CSV/TXT/XLSX برای ساخت گروهی
- `export/ExportManager.kt` — ذخیره PNG/PDF/SVG و صفحه لیبل A4
- `billing/BillingManager.kt` — اشتراک Pro
- `util/NumberFormatter.kt` — جداکردن سه‌رقمی عدد و قیمت
- `data/PreferencesRepository.kt` — تنظیمات، پروفایل و تاریخچه محلی
- `update/UpdateChecker.kt` — بررسی نسخه جدید
- `docs/SOURCE_GUIDE_FA.md` — راهنمای فارسی فایل‌ها و معماری

## نکته امضا و بروزرسانی

برای اینکه نسخه‌های بعدی روی نسخه نصب‌شده Update شوند، `applicationId` نباید تغییر کند و تمام Releaseها باید با **همان Release Key** امضا شوند. کلید خصوصی Release در GitHub عمومی Commit نمی‌شود و فقط در بسته خصوصی سورس تحویلی نگه‌داری می‌شود.

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

در CI این پروژه Gradle 8.9، JDK 17 و Android SDK 35 استفاده می‌شود.

## محدودیت‌های Backend

Dynamic QR واقعی، Analytics ابری، Cloud Sync و اعتبارسنجی سروری خرید نیازمند Backend هستند. رابط فعلی برای این توسعه‌ها آماده است، اما تا زمان انتخاب و اتصال Backend نباید این موارد «فعال» یا «کامل» تلقی شوند.
