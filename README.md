# QR ساز و Barcode ساز — Android v1.0.0

اپ اندروید Kotlin + Jetpack Compose برای ساخت، طراحی، اسکن و خروجی QR Code و Barcode با رابط RTL و ظاهر کیوت.

## وضعیت نسخه 1.0

- ساخت QR برای URL، متن، Wi-Fi، ایمیل، تلفن و SMS
- ساخت Barcode در فرمت‌های Code 128، Code 39، EAN-13، EAN-8، UPC-A، ITF، Codabar، Data Matrix، PDF417 و Aztec
- استایل QR کلاسیک رایگان + Rounded / Dots / Bubble حرفه‌ای
- اسکن QR/Barcode با دوربین
- خروجی PNG معمولی رایگان
- خروجی PNG HD، PDF و SVG در سطح Pro
- Paywall اشتراک هفتگی با product id: `qr_pro_weekly`
- تاریخچه محلی
- منوی همبرگری RTL از سمت راست
- تنظیمات اعلان‌ها
- صفحات معرفی به دوستان، درباره ما، تماس با ما و درباره نرم‌افزار
- Dark Mode هماهنگ با سیستم
- بررسی نسخه جدید از `distribution/latest.json`

## مدل درآمدی

ساخت و پیش‌نمایش کدها رایگان است. خروجی PNG استاندارد برای طرح کلاسیک رایگان می‌ماند. وقتی کاربر استایل حرفه‌ای انتخاب کند یا خروجی HD/PDF/SVG بخواهد، اپ او را به اشتراک هفتگی هدایت می‌کند. کاربر برای هر خروجی جداگانه پول نمی‌دهد؛ تا زمانی که اشتراک فعال است خروجی‌های حرفه‌ای باز هستند.

## پرداخت

نسخه فعلی Adapter پرداخت Google Play Billing 9.1.0 را دارد و شناسه محصول اشتراک هفتگی `qr_pro_weekly` است. برای انتشار روی کافه‌بازار یا مایکت، Provider پرداخت باید به SDK همان فروشگاه متصل شود، ولی UI و Premium Gate مستقل باقی می‌ماند.

قبل از انتشار عمومی، اعتبار خریدها باید سمت سرور هم Verify شود. پیاده‌سازی فعلی Client-side برای آماده‌سازی UI و فلو اولیه است.

## بروزرسانی و امضا

`applicationId` اصلی از نسخه اول ثابت است:

`com.waxew.qrbarcode`

Debug از `com.waxew.qrbarcode.debug` استفاده می‌کند. یک debug keystore ثابت داخل پروژه وجود دارد تا APKهای تستی GitHub Actions روی نسخه تست قبلی نصب شوند و هر Build امضای متفاوت نداشته باشد. این کلید فقط برای تست است و نباید برای انتشار Store استفاده شود.

Release key عمداً داخل GitHub ذخیره نمی‌شود. برای Build امضاشده Production، Secrets زیر در GitHub Actions تنظیم شوند:

- `QR_KEYSTORE_BASE64`
- `QR_KEYSTORE_PASSWORD`
- `QR_KEY_ALIAS`
- `QR_KEY_PASSWORD`

استفاده از همان Release key در تمام نسخه‌های آینده برای نصب آپدیت روی نسخه قبلی الزامی است.

## Build

نیازمندی‌های فعلی:

- Android Gradle Plugin 9.3.0
- Gradle 9.5.0
- Kotlin 2.3.21
- JDK 17
- compileSdk 37
- targetSdk 36
- minSdk 23

Build محلی:

```bash
gradle :app:assembleDebug
```

خروجی:

`app/build/outputs/apk/debug/app-debug.apk`

## آپدیت‌خور بودن

نسخه‌های آینده باید `applicationId` و Release signing key را حفظ کنند و فقط `versionCode` و `versionName` افزایش پیدا کند. فایل `distribution/latest.json` مرجع بررسی نسخه جدید درون برنامه است.
