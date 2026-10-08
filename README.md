# WearOsZapya

WearOsZapya is a file manager and high-speed file transfer app for Wear OS and Android, inspired by Zapya and SHAREit.

Standard Wear OS file transfers run over Bluetooth, which caps speeds at roughly 20-50 KB/s. WearOsZapya adds a high-speed Wi-Fi and hotspot transfer mode that reaches 10-30 MB/s, while keeping Bluetooth as a reliable fallback. If you start a transfer over Bluetooth and Wi-Fi becomes available, the app switches transports automatically and continues from the exact byte where it left off.

## Features

- **Wi-Fi and hotspot transfers**: Sends files over a direct TCP socket connection at 10-30 MB/s. The app requests `NetworkCapabilities.TRANSPORT_WIFI` on Wear OS to wake the Wi-Fi radio on demand.
- **Mid-transfer switching with byte-level resume**: Transfers begin immediately over Bluetooth. When Wi-Fi or a hotspot connection is detected, the receiver confirms the received byte count, and the sender seeks to that exact offset. If Wi-Fi drops, the transfer falls back to Bluetooth without restarting.
- **Wear OS file manager**:
  - Browse directories with rotary dial and touch support.
  - Cut, copy, paste, delete, and rename files.
  - Pin frequently used folders and files to the home screen.
  - Open images, text files, and PDFs directly in built-in viewers.
  - Quick-filter categories for music, photos, videos, and received files.
- **Transfer controls**: Displays active transfer speed in MB/s, progress percentage, and transport type.

## How transport switching works

```
 Phone (FileTransferService)                           Watch (FileReceiverService)
             |                                                      |
             |--- 1. Bluetooth transfer starts (control channel) -->|
             |                                                      |
             |<-- 2. Watch connects to Wi-Fi, sends IP and offset --|
             |                                                      |
             |=== 3. High-speed direct TCP stream on port 8988 ====>|
             |       (Seeks to byte offset in .part file)           |
             |                                                      |
             |--- 4. Falls back to Bluetooth if Wi-Fi drops ------->|
```

## Donations

To support development of this project, you can donate using the following addresses:

| Network / Asset | Address |
| :--- | :--- |
| **Ethereum (ETH)** | `0xA54a491a40e57229f66Fa17f5C6c7f461b262c1d` |
| **Bitcoin (BTC)** | `bc1q78zrnqxes8gdg8c9l34z4sg5258xpsz72at23x` |
| **Tether (USDT TRC20)** | `TVfsR7tHZvbKGK9gMasXFxcoGL43yNG6jV` |

Scannable QR codes for each address are also included inside the app under Settings > Support Me.

## Contact

- Email: [basty.oliva2011@gmail.com](mailto:basty.oliva2011@gmail.com)

## Permissions

Managing files across device storage on Wear OS requires the `MANAGE_EXTERNAL_STORAGE` permission. On Wear OS versions that do not expose this toggle in system settings, grant it via ADB:

```bash
adb shell appops set --uid com.bastyoliva.wearoszapya MANAGE_EXTERNAL_STORAGE allow
```

Standard media categories (photos, music, videos, and received files) work without this permission.

## License

WearOsZapya is licensed under the Apache License 2.0.
