#!/usr/bin/env bash
#
# setup-wireguard-server.sh
# Sets up a WireGuard VPN server with AdGuard Home on Ubuntu/Debian.
#
# Usage: sudo ./setup-wireguard-server.sh
#
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Please run as root or with sudo."
  exit 1
fi

echo "==> Updating system packages..."
apt update && apt upgrade -y

echo "==> Installing WireGuard and dependencies..."
apt install -y wireguard qrencode ufw fail2ban

echo "==> Enabling IP forwarding..."
sysctl -w net.ipv4.ip_forward=1
if ! grep -q "net.ipv4.ip_forward=1" /etc/sysctl.conf; then
  echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
fi

echo "==> Configuring firewall..."
ufw allow 51820/udp
ufw allow OpenSSH
ufw --force enable

echo "==> Creating WireGuard key directory..."
mkdir -p /etc/wireguard/keys
chmod 700 /etc/wireguard/keys

echo "==> Generating server keys..."
wg genkey | tee /etc/wireguard/keys/server_private.key | wg pubkey > /etc/wireguard/keys/server_public.key
SERVER_PRIV=$(cat /etc/wireguard/keys/server_private.key)
SERVER_PUB=$(cat /etc/wireguard/keys/server_public.key)

echo "==> Detecting public IP..."
PUBLIC_IP=$(curl -s https://api.ipify.org || curl -s https://ifconfig.me || echo "YOUR_SERVER_IP")
echo "Detected public IP: $PUBLIC_IP"

read -p "Enter your server domain or IP (default: $PUBLIC_IP): " SERVER_INPUT
SERVER_ENDPOINT=${SERVER_INPUT:-$PUBLIC_IP}

read -p "Enter WireGuard port (default: 51820): " WG_PORT
WG_PORT=${WG_PORT:-51820}

read -p "Enter VPN subnet (default: 10.8.0.0/24): " VPN_SUBNET
VPN_SUBNET=${VPN_SUBNET:-10.8.0.0/24}

echo "==> Creating WireGuard configuration..."
cat <<EOF > /etc/wireguard/wg0.conf
[Interface]
Address = 10.8.0.1/24
ListenPort = $WG_PORT
PrivateKey = $SERVER_PRIV
PostUp = iptables -A FORWARD -i %i -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i %i -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

# Peers will be added here by add-peer.sh
EOF

echo "==> Starting WireGuard..."
systemctl enable wg-quick@wg0
systemctl start wg-quick@wg0

echo "==> Installing AdGuard Home..."
curl -s -S -L https://raw.githubusercontent.com/AdguardTeam/AdGuardHome/master/scripts/install.sh | sh -s -- -v

echo "==> Configuring AdGuard Home DNS..."
cat <<EOF > /opt/AdGuardHome/AdGuardHome.yaml
dns:
  bind_hosts:
    - 10.8.0.1
  port: 53
  statistics_interval: 1
  querylog:
    enabled: true
  filtering:
    blocking_mode: nxdomain
  services:
    - 8.8.8.8:53
    - 8.8.4.4:53
EOF

echo "==> Restarting AdGuard Home..."
systemctl restart AdGuardHome

echo ""
echo "=========================================="
echo "  Server setup complete!"
echo "=========================================="
echo ""
echo "Server public key: $SERVER_PUB"
echo "Server endpoint: $SERVER_ENDPOINT:$WG_PORT"
echo "VPN subnet: $VPN_SUBNET"
echo "AdGuard Home DNS: 10.8.0.1"
echo ""
echo "Next steps:"
echo "1. Add clients using: sudo ./add-peer.sh <phone-name>"
echo "2. Configure your Android app with server endpoint"
echo "3. Import the generated QR code or .conf file into WireGuard app"
echo ""
echo "Client config DNS will point to AdGuard Home at 10.8.0.1 for ad blocking"
echo "=========================================="
