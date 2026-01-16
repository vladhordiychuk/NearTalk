# NearTalk – Offline Bluetooth Mesh Messenger

NearTalk is a decentralized, fully offline messenger application for Android written in **Kotlin**.
It allows users to communicate peer-to-peer using a custom **Bluetooth Mesh Protocol**, enabling communication even beyond direct connection range by routing messages through other devices.

Perfect for festivals, underground areas, or emergency situations with no Internet connection or cellular coverage.

> **Current Status:** v1.1-mesh Release 🕸️

---

## ✨ Features

### 🕸️ Mesh Network (New!)
- **Multi-hop Routing:** Messages can "jump" through intermediate devices to reach users who are out of direct range (up to 5 hops/TTL).
- **Dynamic Routing Table:** The app automatically discovers paths to distant devices and updates the routing table in real-time.
- **Smart Flooding:** Uses a controlled flooding algorithm with Time-To-Live (TTL) to ensure message delivery without clogging the network.

### 📱 Core Messaging
- **Broadcast & Private Channels:** Send public messages to everyone nearby ("Shout") or secure private messages to specific users.
- **Offline Communication:** Fully functional without Internet or SIM card.
- **Real-time Typing Sync:** See what the other person is typing character-by-character (synchronous input).
- **Message Integrity:** Hop-to-hop hash verification ensures messages aren't corrupted during relay.

### 🎨 UI & Experience
- **Modern UI:** Built with **Jetpack Compose** (Material 3).
- **Theming:** Full support for Dark and Light themes.
- **Zero Configuration:** Just open the app, grant permissions, and you are part of the mesh.

---

## 🛠️ Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Concurrency:** Coroutines / Flow / Channels
- **Connectivity:** Android Bluetooth API (RFCOMM)
- **Data Serialization:** Kotlinx Serialization (JSON)

---

## 📸 Screenshots

### 🌑 Dark Theme
<img src="https://github.com/user-attachments/assets/fce488c0-86cb-479d-ac1e-cccebbfc926f" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/c3f59a09-83e6-4d3b-a66d-1cbf7189c329" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/1f7feb31-e0cc-48db-9891-3305c2cc342c" width="260" height="520" />

### ☀️ Light Theme
<img src="https://github.com/user-attachments/assets/d57e96ec-dfe2-4191-b153-467a561d50c7" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/9790d4b8-6788-4fa7-96a1-43363cb1f810" width="260" height="520" /> <img src="https://github.com/user-attachments/assets/3a56cad1-c218-4331-aeb3-61d7863687c7" width="260" height="520" />

---

## 📦 How to Install
You can download the latest APK file from the [Releases](../../releases) page.

1. **Download** `NearTalk.apk`.
2. **Install** the application (allow installation from unknown sources if prompted).
3. **⚠️ Important Permission Step:**
   Before finding devices, you must grant permissions. The app requires:
   * **Location (Fine Location):** Required by Android to scan for Bluetooth devices.
   * **Bluetooth (Nearby Devices):** To connect with others.
   *(Please grant these permissions in Settings or when prompted).*
4. **Run** the app, wait for automatic discovery, and start chatting!

---

## 🚀 Future Plans
- [ ] End-to-End Encryption (AES)
- [ ] Image and file sharing over Mesh
- [ ] Background connection stability improvements (WakeLocks)
- [ ] Bluetooth Low Energy (BLE) support for battery saving

---

## 📄 License
This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
