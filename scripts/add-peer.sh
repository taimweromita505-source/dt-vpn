#!/usr/bin/env bash
#
# add-peer.sh
# Registers a new client (phone) as a WireGuard peer and prints/QRs
# a client config pointing DNS at AdGuard Home on this server.
#
# Usage: sudo ./add-peer.sh phone-name
#
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <peer-name>"
  exit 1
fi

PEER_NAME=$1
WG_IF="wg0"
KEY_DIR="/etc/wireguard/keys/peers"
mkdir -p "$KEY_DIR"

SERVER_PUB=$(cat /etc/wireguard/keys/server_public.key)
SERVER_ENDPOINT=$(grep -oP 'Endpoint\s*=\s*\K[^:]+' /etc/wireguard/wg0.conf || echo "YOUR_SERVER_IP")
WG_PORT=$(grep -oP 'ListenPort\s*=\s*\K[0-9]+' /etc/wireguard/wg0.conf || echo "51820")
ADGUARD_IP="10.8.0.1"

LAST_OCTET=$(grep -oP '10\.8\.0\.\K[0-9]+' /etc/wireguard/wg0.conf | sort -n | tail -1)
NEXT_OCTET=$(( ${LAST_OCTET:-1} + 1 ))
PEER_IP="10.8.0.${NEXT_OCTET}"

echo "==> Generating keys for ${PEER_NAME}"
wg genkey | tee "${KEY_DIR}/${PEER_NAME}_private.key" | wg pubkey > "${KEY_DIR}/${PEER_NAME}_public.key"
PEER_PRIV=$(cat "${KEY_DIR}/${PEER_NAME}_private.key")
PEER_PUB=$(cat "${KEY_DIR}/${PEER_NAME}_public.key")

echo "==> Registering peer with running WireGuard interface"
wg set ${WG_IF} peer "${PEER_PUB}" allowed-ips "${PEER_IP}/32"

echo "==> Persisting peer to wg0.conf"
cat <<EOF >> /etc/wireguard/wg0.conf

[Peer]
# ${PEER_NAME}
PublicKey = ${PEER_PUB}
AllowedIPs = ${PEER_IP}/32
EOF

echo "==> Writing client config"
CLIENT_CONF="/root/${PEER_NAME}.conf"
cat <<EOF > "${CLIENT_CONF}"
[Interface]
PrivateKey = ${PEER_PRIV}
Address = ${PEER_IP}/32
DNS = ${ADGUARD_IP}

[Peer]
PublicKey = ${SERVER_PUB}
Endpoint = ${SERVER_ENDPOINT}:${WG_PORT}
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25
EOF

echo ""
echo "Client config written to ${CLIENT_CONF}"
echo "Scan this QR code in the WireGuard app, or import the .conf file:"
if command -v qrencode &> /dev/null; then
  qrencode -t ansiutf8 < "${CLIENT_CONF}"
else
  echo "Install qrencode for QR code generation"
  cat "${CLIENT_CONF}"
fi
