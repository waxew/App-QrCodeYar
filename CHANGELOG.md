# Changelog

## 2.0.0 (versionCode 12)

- QR Designer 2.0: رنگ مستقل Finder، جهت گرادیان افقی/عمودی/مورب، Module Scale، تصویر پس‌زمینه، شکل و حاشیه لوگو
- ذخیره و بارگذاری Presetهای طراحی؛ حداکثر 20 Preset محلی
- Backup schema 3 شامل History/Folder/Tag + Settings + Design Presets؛ بدون PIN hash، Session یا Cloud token
- Barcode Label Studio 2.0 با انتخاب Code128/Code39/EAN13/EAN8/DataMatrix، ابعاد سفارشی، نمایش/عدم نمایش قیمت و کد، PNG/PDF
- Scanner 2.0 با مدیریت صحیح lifecycle منابع CameraX/ML Kit، Zoom، Torch، Continuous و Duplicate prevention تا 500 نتیجه
- Start Page Scanner با ActivityResult واقعی؛ نتیجه اسکن صفحه شروع در History ثبت می‌شود
- اجرای واقعی گزینه تأیید قبل از بازکردن لینک اسکن‌شده
- Batch 2.0: CSV/TXT/XLSX تا 500 ردیف، Mapping ستون، PNG گروهی، ZIP مستقیم، PDF A4/A5 با ستون/ردیف سفارشی
- افزایش ظرفیت History محلی از 100 به 500 رکورد بدون تغییر schema Room و بدون حذف داده موجود
- قفل Biometric/Device Credential در کنار PIN محلی؛ داده بیومتریک داخل برنامه ذخیره نمی‌شود
- Cloud Client امن و Backend-ready با Supabase Auth، Dynamic QR، Sync دوطرفه History و Analytics
- اضافه‌شدن `backend/supabase/schema.sql` با RLS مالک‌محور و Edge Function عمومی `resolve-qr` برای Redirect
- Publishable Key تنها کلید مجاز در APK است؛ Service Role فقط سمت Edge Function باقی می‌ماند
- applicationId ثابت `com.waxew.qrbarcode` برای حفظ Update path

## 1.9.2 (versionCode 11)

- تکمیل نهایی Roadmap آفلاین نسخه‌های 1.2 تا 1.9
- Compact Mode واقعی با کاهش کنترل‌شده Density رابط Compose
- Smart Templateها از حالت کاتالوگ به Payload Builder واقعی برای Wi-Fi، vCard، رستوران، شبکه اجتماعی، محصول و موقعیت تبدیل شدند
- Backup schema 2 با حفظ Folder/Tag و تنظیمات
- Restore امن تنظیمات با لیست سفید کلیدها و بدون بازیابی خودکار PIN
- Restore واقعی History/Folder/Tag داخل Room با sanitize و deduplicate
- BackupRestoreActivity برای بازکردن فایل JSON از File Manager و بازیابی محلی
- سخت‌گیری بیشتر Link Security برای localhost، IP، Punycode، لینک کوتاه، @ و Schemeهای پرخطر
- Product Label Renderer با ابعاد متغیر و اعتبارسنجی محدوده چاپ
- CI و Artifactها به 1.9.2 هماهنگ شدند
- applicationId ثابت ماند تا Update روی نسخه قبلی نصب شود

## 1.9.1 (versionCode 10)

- تکمیل اتصال قابلیت‌های نیمه‌کامل 1.9 به جریان واقعی برنامه
- Folder و Tag واقعی روی Room Database با Migration امن از schema نسخه 1 به 2 و بدون حذف تاریخچه قبلی
- Archive Manager برای جستجو، فیلتر پوشه و ویرایش Folder/Tag هر رکورد
- Accent واقعی Material 3 با سه پالت صورتی یاسی، سبز نعنایی و آبی آسمانی
- Start Page عملی با انتخاب خانه، اسکنر یا مرکز 1.9
- Scanner جدید متصل به تنظیمات Beep، Vibrate، Continuous Scan و Prevent Duplicates
- حفظ Scanner قبلی برای سازگاری سورس
- حذف Reflection از بکاپ محلی و استفاده مستقیم از application context امن Repository
- اصلاح خطای Compose scoped weight در Scanner جدید
- applicationId بدون تغییر برای نصب Update روی نسخه قبلی

## 1.9.0 (versionCode 9)

- تجمیع Roadmap نسخه‌های 1.2 تا 1.9 در یک نسخه
- مرکز امکانات 1.9 روی برنامه اصلی بدون حذف قابلیت‌های قبلی
- Smart Template Catalog برای Wi-Fi، کارت ویزیت، رستوران، شبکه اجتماعی، محصول و موقعیت
- Barcode Studio فروشگاهی با ساخت پیش‌نمایش لیبل شامل نام، قیمت و کد کالا
- سیاست امنیت URL پیشرفته‌تر: HTTP، IP مستقیم، Punycode، @ و لینک‌های کوتاه‌شده
- تنظیمات اسکنر برای Beep، لرزش، اسکن متوالی، جلوگیری از تکرار و تأیید قبل از بازکردن لینک
- بکاپ محلی JSON از تاریخچه و تنظیمات 1.9
- مدل توسعه Archive Folder و Tag برای اتصال به Room
- شخصی‌سازی Compact Mode، Accent و Start Page در Repository نسخه 1.9
- قفل برنامه با PIN چهار تا هشت رقمی؛ فقط SHA-256 PIN ذخیره می‌شود
- حفظ QR Studio، CameraX + ML Kit، اسکن گالری، Room History، Batch CSV/TXT/XLSX و PDF لیبل A4 نسخه قبل
- applicationId ثابت برای حفظ مسیر Update

## 1.1.0 (versionCode 3)

- قالب‌بندی سه‌رقمی قیمت‌ها
- QR Studio: Gradient، Logo، Finder Style، Frame، Transparent Background، Undo/Redo
- Readability/Contrast checker
- QR payloadهای vCard/Event/Geo/Social
- Scanner زنده CameraX + ML Kit با Torch، Zoom و Multi-code
- اسکن از Gallery و Multi-code image scan
- لینک‌سنج آفلاین برای URLهای اسکن‌شده
- مهاجرت History از JSON/SharedPreferences به Room با حفظ داده نسخه 1.0.1
- Batch QR از CSV/TXT/XLSX
- A4 label PDF
- History search/filter/favorite/delete
- Drawer جدید با پروفایل کاربر و ابزارهای اختصاصی

## 1.0.1 (versionCode 2)

- Back stack داخلی
- Drawer RTL
- About ساده و کاربرپسند
- رفع باگ‌های خروجی و لینک‌ها
- مستندسازی سورس
