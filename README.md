# Taimwe VPN

A free, powerful Android VPN app with built-in ad blocker, built with Kotlin, Jetpack Compose, and WireGuard.

## Features

- **Real WireGuard integration** with config import and QR scanning support
- **Advanced Ad Blocker** with 20+ hardcoded ad domains and remote block list support
- **Beautiful animated UI** with neon dark theme, gradients, and smooth transitions
- **Server selection** with multiple country options
- **Connection statistics** tracking
- **Connection history** with past connection logs
- **Foreground service** for persistent VPN connection
- **WireGuard config generator** for easy server setup
- **Config import** via text paste or QR code scan
- **Settings screen** with ad blocker toggle, auto-connect, notifications, and dark mode

## Project Structure

- `MainActivity.kt` - Animated Compose UI with VPN permission handling and config import
- `VpnService.kt` - WireGuard VPN service with config parsing
- `WireGuardConfig.kt` - WireGuard configuration generator
- `ConfigImporter.kt` - WireGuard config file/text/QR importer
- `ImportConfigActivity.kt` - Config import screen
- `QrScannerActivity.kt` - QR code scanner for WireGuard configs
- `SettingsActivity.kt` - App settings screen
- `ConnectionHistoryActivity.kt` - Connection history and logs
- `AdBlocker.kt` - Ad domain blocking engine with remote list fetching
- `VpnViewModel.kt` - State management for UI and VPN connection
- `PreferencesManager.kt` - Local settings persistence
- `scripts/setup-wireguard-server.sh` - Server setup automation
- `scripts/add-peer.sh` - Client peer registration with QR codes

## Android App Build

```bash
cd C:\Users\Taimwe\projects\vpn-app
./gradlew assembleDebug
```

## Server Setup

On your VPS (Ubuntu/Debian), run:

```bash
cd C:\Users\Taimwe\projects\vpn-app\scripts
chmod +x setup-wireguard-server.sh add-peer.sh
scp setup-wireguard-server.sh root@YOUR_VPS_IP:/root/
ssh root@YOUR_VPS_IP "bash /root/setup-wireguard-server.sh"
```

Follow the prompts to configure:
- Server domain/IP
- WireGuard port
- VPN subnet

## Adding Clients

After server setup, add a new phone/client:

```bash
ssh root@YOUR_VPS_IP "bash /root/add-peer.sh phone-name"
```

This will:
1. Generate keys for the client
2. Assign an IP from the VPN subnet
3. Create a client config file
4. Print a QR code for easy import

## How to Use the App

1. **Build and install** the APK on your Android device
2. **Get a WireGuard config** from your server using `add-peer.sh`
3. **Import the config**:
   - Tap "Import Config" and paste the config text
   - Or scan the QR code from the server
4. **Connect**: Tap "Connect" and accept the VPN permission
5. **Ad blocking** works automatically via DNS through AdGuard Home
6. **View history**: Check connection history in the history screen
7. **Settings**: Customize ad blocker, auto-connect, notifications, and dark mode

## Config Format

The app accepts standard WireGuard config format:

```ini
[Interface]
PrivateKey = <your-private-key>
Address = 10.8.0.2/32
DNS = 10.8.0.1

[Peer]
PublicKey = <server-public-key>
Endpoint = your-server.com:51820
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25
```

## Note

This app uses the WireGuard Android library for real tunneling. For production deployment:
- A WireGuard server is required
- App signing is needed for Play Store distribution
- Additional permissions and battery optimizations may be needed
