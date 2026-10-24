# BControl

แอป Android สำหรับตั้งค่าความสว่างหน้าจอแบบ preset บนอุปกรณ์ Huawei (EMUI) ที่ค่าความสว่างจากแอปอื่นถูกรีเซ็ตทุกครั้งที่ล็อก/ปลดล็อกจอ

## ปัญหาและสาเหตุ

แอปปรับความสว่างทั่วไปเขียนค่าแค่ `Settings.System.SCREEN_BRIGHTNESS` แต่ EMUI ใช้ค่าจาก key ของตัวเอง `hw_screen_temp_brightness` (ช่วง 0–255) ตอนเปิดจอ ซึ่ง key นี้เปลี่ยนเฉพาะเมื่อเลื่อน slider ของระบบ ความสว่างจึงเด้งกลับทุกครั้งหลังล็อกจอ

BControl เขียนค่าลงทั้งสอง key ([`Brightness.kt`](app/src/main/java/com/sumetph/bcontrol/Brightness.kt)) ค่าจึงคงอยู่หลังล็อก/ปลดล็อก

ทดสอบแล้วบน Huawei MatePad T8 (KOB2-W09), EMUI 10.1, Android 10

## ฟีเจอร์

- **Popup** มีแถบเลื่อน ปุ่ม − / + (ทีละ 1) และ preset 4 ช่อง แตะเพื่อใช้ค่า กดค้างเพื่อบันทึกค่าปัจจุบัน
- **แผงในแถบแจ้งเตือน** แสดงระดับความสว่าง มีปุ่ม − / + และ preset แตะตัวการแจ้งเตือนเพื่อเปิด popup
- แผงแจ้งเตือนแสดงขึ้นใหม่เองหลัง reboot

## ข้อจำกัด

- **`targetSdk = 22` โดยตั้งใจ** — Android ไม่ยอมให้แอปที่ target 23 ขึ้นไปเขียน system key ที่ไม่ใช่ public (เช่น `hw_screen_temp_brightness`) แม้จะได้ `WRITE_SECURE_SETTINGS` ก็ตาม (ทดสอบแล้ว) ผลคือ:
  - ติดตั้งได้บน Android 10–13 (อาจมีคำเตือนว่าสร้างสำหรับ Android รุ่นเก่า)
  - **Android 14 ขึ้นไปติดตั้งไม่ได้** และขึ้น Google Play ไม่ได้
- HarmonyOS NEXT รัน APK ไม่ได้
- EMUI รุ่นอื่นอาจใช้ชื่อ key ต่างไป ตรวจได้ด้วยการ diff `adb shell settings list system` ก่อนและหลังเลื่อน slider ของระบบ
- Notification ไม่รองรับ SeekBar แผงแจ้งเตือนจึงมีแค่ปุ่ม และตัวเลขบนแผงจะไม่อัปเดตตามเมื่อเลื่อน slider ของระบบ จนกว่าจะกดปุ่มในแผง

## Build และติดตั้ง

ต้องมี Android SDK (compileSdk 36) และ JDK 17 ขึ้นไป

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

บน Huawei ต้องกดยืนยันการติดตั้งบนจอ หรือปิด **ตัวเลือกนักพัฒนา → ตรวจสอบแอปที่ติดตั้งผ่าน ADB**

## การตั้งค่าบนเครื่อง

1. สิทธิ์ `WRITE_SETTINGS` ได้มาอัตโนมัติตอนติดตั้ง (เพราะ targetSdk 22) ถ้าไม่ได้ แอปจะมีปุ่มพาไปหน้าอนุญาต
2. เปิดแอปหนึ่งครั้งเพื่อให้แผงแจ้งเตือนขึ้น
3. ถ้าต้องการให้แผงขึ้นเองหลัง reboot: **ตั้งค่า → แบตเตอรี่ → เปิดใช้แอป** ตั้ง BControl เป็น "จัดการด้วยตนเอง" และเปิด "เปิดใช้อัตโนมัติ"

## โครงสร้าง

| ไฟล์ | หน้าที่ |
|---|---|
| `Brightness.kt` | อ่าน/เขียนความสว่าง (ทั้ง `screen_brightness` และ `hw_screen_temp_brightness`) |
| `Presets.kt` | เก็บค่า preset ใน SharedPreferences |
| `MainActivity.kt` | Popup (ธีม dialog) |
| `Panel.kt` | Notification แบบ ongoing และ `PanelReceiver` รับการกดปุ่ม / `BOOT_COMPLETED` |
| `res/layout/panel.xml` | Layout ของแผงแจ้งเตือน |
