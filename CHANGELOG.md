# Changelog

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
