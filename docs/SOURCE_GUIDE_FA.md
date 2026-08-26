# راهنمای سورس App-QrCodeYar

این فایل برای پیدا کردن سریع مسئولیت هر بخش نوشته شده است. علاوه بر این راهنما، داخل خود فایل‌های Kotlin/XML/Gradle نیز کامنت‌های توضیحی قرار دارد.

## فایل‌های ریشه

- `settings.gradle.kts`: نام پروژه، مخازن وابستگی و معرفی ماژول `app`.
- `build.gradle.kts`: نسخه پلاگین Android و Kotlin.
- `gradle.properties`: تنظیم AndroidX و گزینه‌های Gradle.
- `.gitignore`: جلوگیری از Commit فایل‌های build، کلیدهای خصوصی و فایل‌های IDE.

## ماژول app

- `app/build.gradle.kts`: applicationId، نسخه، SDK، build typeها، signing و dependencyها.
- `AndroidManifest.xml`: مجوزها و تعریف MainActivity.
- `MainActivity.kt`: نقطه ورود، ساخت BillingManager/PreferencesRepository و راه‌اندازی Compose.

## منطق برنامه

- `ui/QrBarcodeApp.kt`: صفحه خانه، QR، Barcode، Scanner، Template، History، Premium، Settings، About، Drawer و Back stack.
- `generator/CodeGenerator.kt`: تبدیل payload به BitMatrix و Bitmap با ZXing.
- `export/ExportManager.kt`: ذخیره PNG/PDF/SVG با MediaStore.
- `data/PreferencesRepository.kt`: تاریخچه و تنظیمات محلی.
- `billing/BillingManager.kt`: اتصال Google Play Billing و وضعیت اشتراک Pro.
- `update/UpdateChecker.kt`: خواندن `distribution/latest.json` و نمایش بروزرسانی در نسخه قدیمی‌تر.
- `ui/theme/Theme.kt`: رنگ‌های روشن/تیره Material3.

## قانون Back

هر تغییر صفحه باید از تابع `navigateTo()` عبور کند. این تابع مقصد فعلی را در `backStack` ذخیره می‌کند. `BackHandler` به‌ترتیب Drawer را می‌بندد، به صفحه قبلی برمی‌گردد، و فقط در HOME کنترل Back را به Android می‌دهد.

## قانون انتشار نسخه بعدی

1. `applicationId` تغییر نکند.
2. `versionCode` حتماً افزایش یابد.
3. `versionName` افزایش یابد.
4. APK/AAB با همان Release Key نسخه قبلی امضا شود.
5. `distribution/latest.json` با نسخه و changelog جدید به‌روزرسانی شود.
6. Release Key هرگز وارد GitHub عمومی نشود.
