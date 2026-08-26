# App-QrCodeYar

اپلیکیشن اندرویدی ساخت، شخصی‌سازی، اسکن و خروجی گرفتن از QR Code و Barcode با رابط فارسی و طراحی سبک و فانتزی.

## نسخه فعلی

- Version: **1.0.1**
- Version code: **2**
- Package/Application ID: `com.waxew.qrbarcode`
- Minimum Android: API 23
- Target Android: API 35
- Kotlin + Jetpack Compose

## امکانات

- ساخت QR برای لینک، متن، Wi-Fi، ایمیل، تلفن و SMS
- ساخت Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- اسکن QR و Barcode با دوربین
- استایل‌های کلاسیک، گرد، نقطه‌ای و حبابی
- خروجی PNG، PNG HD، PDF و SVG
- تاریخچه محلی
- تنظیم اعلان‌ها و Dark Mode خودکار
- منوی همبرگری راست‌چین
- Back stack داخلی: دکمه Back ابتدا به صفحه قبلی برمی‌گردد و فقط از خانه می‌تواند برنامه را ببندد
- بررسی نسخه جدید از `distribution/latest.json` همین ریپو
- مدل Freemium و اشتراک هفتگی Pro

## ساختار مهم سورس

- `app/src/main/java/com/waxew/qrbarcode/ui/QrBarcodeApp.kt` — صفحه‌ها، Drawer و ناوبری
- `generator/CodeGenerator.kt` — موتور QR/Barcode
- `export/ExportManager.kt` — ذخیره PNG/PDF/SVG
- `billing/BillingManager.kt` — اشتراک Pro
- `data/PreferencesRepository.kt` — تنظیمات و تاریخچه محلی
- `update/UpdateChecker.kt` — بررسی نسخه جدید
- `docs/SOURCE_GUIDE_FA.md` — راهنمای فارسی فایل‌ها و معماری

## نکته امضا و بروزرسانی

برای انتشار واقعی، تمام نسخه‌های بعدی باید با **همان Release Key** و همان `applicationId` امضا شوند و فقط `versionCode`/`versionName` افزایش پیدا کند. کلید خصوصی Release نباید در GitHub عمومی Commit شود. اطلاعات کلید انتشار همراه بسته سورس تحویلی در `info.txt` نگه‌داری می‌شود.

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

GitHub Actions نیز در `.github/workflows/android.yml` Build را کنترل می‌کند.
