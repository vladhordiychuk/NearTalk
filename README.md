# NearTalk – Offline Bluetooth Messenger

NearTalk is a fully offline messenger application for Android written in **Kotlin**.
It allows users to communicate peer-to-peer using **Bluetooth**, making it perfect for situations with no Internet connection or cellular coverage.

> **Current Status:** v1.0 Release.

---

## ✨ Features
- [x] **Offline Communication:** Send and receive messages via Bluetooth (no Internet required).
- [x] **Real-time Typing Sync:** See what the other person is typing character-by-character (synchronous input).
- [x] **Modern UI:** Built with Jetpack Compose.
- [x] **Theming:** Full support for Dark and Light themes.
- [x] **Device Discovery:** Scan and connect to nearby Bluetooth devices.

---

## 🛠️ Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Concurrency:** Coroutines / Flow
- **Connectivity:** Android Bluetooth API

---

## 📸 Screenshots

### 🌑 Dark Theme
<img src="https://github.com/user-attachments/assets/fce488c0-86cb-479d-ac1e-cccebbfc926f" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/c3f59a09-83e6-4d3b-a66d-1cbf7189c329" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/1f7feb31-e0cc-48db-9891-3305c2cc342c" width="260" height="520" />

### ☀️ Light Theme
<img src="https://github.com/user-attachments/assets/d57e96ec-dfe2-4191-b153-467a561d50c7" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/9790d4b8-6788-4fa7-96a1-43363cb1f810" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/3a56cad1-c218-4331-aeb3-61d7863687c7" width="260" height="520" />

---

## 📦 How to Install
You can download the latest APK file from the [Releases](../../releases) page.

1. **Download** `NearTalk_v1.0.apk`.
2. **Install** the application (allow installation from unknown sources if prompted).
3. **⚠️ Important Permission Step:**
   Before finding devices, you must grant permissions. The app requires:
   * **Location (Fine Location):** Required by Android to scan for Bluetooth devices.
   * **Bluetooth (Nearby Devices):** To connect with others.
   *(Please grant these permissions in Settings or when prompted).*
4. **Run** the app, pair with another device, and start chatting!

---

## 🚀 Future Plans
- [ ] Image and file sharing
- [ ] Background connection stability improvements
- [ ] Bluetooth Low Energy (BLE) support
