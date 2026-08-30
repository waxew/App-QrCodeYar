# App-QrCodeYar

اپلیکیشن فارسی Android برای ساخت، طراحی، اسکن، مدیریت، چاپ و همگام‌سازی QR Code و Barcode با رابط RTL و Material 3.

## نسخه فعلی

- Version: **2.0.0**
- Version Code: **12**
- Application ID: `com.waxew.qrbarcode`
- Min SDK: 23
- Target / Compile SDK: 35
- Kotlin + Jetpack Compose + Room + CameraX + ML Kit + ZXing

## قابلیت‌های نهایی

### QR Designer
- QR برای URL، Text، Wi-Fi، Email، Phone، SMS، vCard، Event، Geo و Social
- Module: Classic / Rounded / Dots / Bubble
- Finder مستقل + رنگ Finder مستقل
- Gradient با جهت افقی، عمودی و مورب
- Module Scale برای ضخامت ظاهری
- Background color، Transparent background و Background image
- Logo وسط QR با Square / Rounded / Circle و Border color
- Frame و Label frame
- Undo/Redo، Readability/Contrast checker
- Presetهای ذخیره‌شونده طراحی
- PNG، PNG HD، PDF و SVG

### Barcode / Label Studio
- Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- Product Label با نام، قیمت سه‌رقمی، کد کالا، انتخاب فرمت، ابعاد سفارشی و کنترل نمایش متن
- خروجی PNG/PDF و خروجی استاندارد Barcode در PNG HD/PDF/SVG

### Scanner
- CameraX + ML Kit
- Torch، Zoom، Beep، Vibrate، Continuous Scan، Prevent Duplicates
- اسکن چندکدی از Gallery
- تشخیص نوع محتوا، Copy/Share/Open
- Link Safety آفلاین و اجرای واقعی Confirm Before Opening Links
- مدیریت lifecycle منابع Camera/ML Kit/Executor

### Archive / Backup
- Room History تا 500 رکورد
- Search، Filter، Favorite، Delete، Folder و Tag
- Migration امن Room schema 1 -> 2
- Backup JSON schema 3 به Downloads/QRStudio
- Restore از File Manager
- Backup شامل History/Folder/Tag، Settings و Design Presets است؛ PIN/Token ابری Backup نمی‌شود

### Batch / Print
- CSV/TXT/XLSX تا 500 ردیف
- انتخاب و Mapping ستون Payload
- PNG گروهی
- ZIP گروهی Stream-based
- PDF لیبل A4 و A5 با تعداد ستون/ردیف قابل تنظیم

### Security / Personalization
- PIN محلی با SHA-256
- Biometric / Device Credential بدون ذخیره داده بیومتریک
- Accentهای Material 3، Compact Mode، Start Page
- Drawer RTL، Profile، Back stack صحیح

## Cloud / Dynamic QR 2.0

سورس Android و قرارداد Backend برای این قابلیت‌ها آماده است:

- Supabase Email Auth
- Dynamic QR با Slug ثابت و مقصد قابل تغییر
- Cloud Sync دوطرفه History برای چند دستگاه
- Analytics اسکن با زمان و Country/City در صورت وجود Header زیرساخت
- RLS مالک‌محور برای تمام داده‌های کاربر
- Edge Function `resolve-qr` برای Redirect عمومی QR چاپ‌شده

Backend در `backend/supabase/` قرار دارد. برای فعال‌شدن Cloud باید یک Supabase Project اختصاصی برای QR یار ایجاد و سپس Build با متغیرهای زیر انجام شود:

```text
SUPABASE_URL
SUPABASE_PUBLISHABLE_KEY
DYNAMIC_QR_BASE_URL
```

`service_role` یا Secret Key هرگز نباید داخل APK قرار بگیرد. در حالت بدون Backend، تمام قابلیت‌های آفلاین برنامه مستقل و فعال باقی می‌مانند.

## Build

CI با JDK 17، Gradle 8.9 و Android SDK 35 هر دو `assembleDebug` و `assembleRelease` را می‌سازد.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## امضا و بروزرسانی

`applicationId` ثابت است. برای اینکه APK Production روی نسخه قبلی Update شود، تمام Releaseهای Production باید با همان Release Key اصلی امضا شوند. کلید خصوصی Release در Repository عمومی Commit نمی‌شود.

## فایل‌های مهم

- `ui/QrBarcodeApp.kt` — UI اصلی، QR/Barcode/Scanner/Batch
- `ui/V19Root.kt` — Hub نسخه 2.0، Archive، Settings و Lock
- `ui/V20Panels.kt` — Smart Template، Label Studio و Cloud UI
- `generator/CodeGenerator.kt` — موتور Render QR/Barcode
- `scanner/V19ScannerActivity.kt` — Scanner 2.0
- `data/HistoryDatabase.kt` — Room + Migration
- `v20/V20Cloud.kt` — Auth/Dynamic QR/Sync/Analytics client
- `v20/V20Biometric.kt` — Biometric gate
- `v20/V20DesignPresetStore.kt` — Presetهای طراحی
- `backend/supabase/` — Schema/RLS/Edge Function

## حریم خصوصی

در حالت آفلاین هیچ History، Profile یا QR به سرور ارسال نمی‌شود. Cloud فقط پس از تنظیم Backend و ورود کاربر فعال می‌شود.
