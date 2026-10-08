# WearOsZapya ⚡📱⌚

WearOsZapya is an ultra-fast file transfer and file manager app for Wear OS and Android, inspired by Zapya and SHAREit.

While traditional Wear OS file sharing apps rely solely on Bluetooth (throttled to ~20–50 KB/s), **WearOsZapya** features a **Dual-Mode Turbo Boost Engine** that dynamically switches between Bluetooth and high-speed Wi-Fi / Hotspot (10–30 MB/s) with **zero transfer interruption and byte-offset resuming**.

---

## ⚡ Core Features

- ⚡ **Turbo Boost (Hotspot / Wi-Fi Transfer)**:
  - High-speed direct TCP streaming at 10–30 MB/s.
  - Automatically acquires high-bandwidth Wi-Fi network capability (`NetworkCapabilities.TRANSPORT_WIFI`) on Wear OS, bypassing normal Bluetooth throttling.
- 🔄 **Seamless Mid-Transfer Transport Switching & Byte-Level Resuming**:
  - Start sending over Bluetooth immediately without waiting for Wi-Fi.
  - As soon as Hotspot or local Wi-Fi is detected, transfer switches automatically to Turbo mode.
  - **Transfers never restart**: If 2 MB was transferred via Bluetooth, Turbo Boost connects and continues from exact byte `2,097,152` using atomic seek offsets in `.part` files.
  - If Wi-Fi disconnects mid-transfer, it falls back to Bluetooth seamlessly.
- 📂 **Full-Featured Wear OS File Manager**:
  - Browse, view, open, and delete files on your watch.
  - Clipboard operations: Cut, Copy, Paste.
  - Pin favorite files and folders to the home screen.
  - Built-in media viewers: Photos, Music, Videos, and PDF documents.
- 🎯 **Modern Electric Violet & Amber UI**:
  - Custom watch launcher icons.
  - Real-time transfer speed (`⚡ 18.5 MB/s`), byte progress, and one-tap Turbo activation.

---

## 🏗️ Turbo Boost Architecture

```
 Mobile Phone                                           Wear OS Watch
 [FileTransferService]                                [FileReceiverService]
          │                                                     │
          │────── 1. Start Bluetooth Transfer (Control) ───────>│
          │       (Standard ChannelClient stream)               │
          │                                                     │
          │<───── 2. /zapya/boost/ready (IP, Port, Offset) ─────│ (Requests Wi-Fi
          │                                                     │  via ConnectivityManager)
          │                                                     │
          │====== 3. Turbo High-Speed TCP Stream (8988) =======>│ (10-30 MB/s)
          │       [RandomAccessFile seek(startOffset)]          │
          │                                                     │
          │────── 4. Fallback to Bluetooth if Wi-Fi drops ─────>│ (Zero byte loss)
```

---

## 💎 Crypto Donations

If you appreciate WearOsZapya, you can support continuous development via cryptocurrency:

| Network / Asset | Address |
| :--- | :--- |
| **Ethereum (ETH)** | `0xA54a491a40e57229f66Fa17f5C6c7f461b262c1d` |
| **Bitcoin (BTC)** | `bc1q78zrnqxes8gdg8c9l34z4sg5258xpsz72at23x` |
| **Tether (USDT TRC20)** | `TVfsR7tHZvbKGK9gMasXFxcoGL43yNG6jV` |

*(Donation QR codes are also built directly into the mobile and watch apps under Settings > Support Me)*

---

## 📬 Contact & Support

- **Developer Email**: [basty.oliva2011@gmail.com](mailto:basty.oliva2011@gmail.com)
- **Repository**: [https://github.com/bastyoliva/wearoszapya](https://github.com/bastyoliva/wearoszapya)

---

## ⚠️ File Access Permissions

To manage files on Wear OS outside the app sandbox, grant `MANAGE_EXTERNAL_STORAGE` via ADB if needed:
```bash
adb shell appops set --uid com.bastyoliva.wearoszapya MANAGE_EXTERNAL_STORAGE allow
```
Standard photo, music, video, and received files are accessible without ADB permissions.

---

## 📄 License
WearOsZapya is licensed under the Apache License 2.0.
