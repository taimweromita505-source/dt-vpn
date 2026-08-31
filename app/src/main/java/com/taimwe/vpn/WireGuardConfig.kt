package com.taimwe.vpn

object WireGuardConfig {
    fun generateConfig(
        privateKey: String,
        publicKey: String,
        endpoint: String,
        allowedIPs: String = "0.0.0.0/0, ::/0",
        dns: String = "10.8.0.1, 1.1.1.1"
    ): String {
        return """
[Interface]
PrivateKey = $privateKey
Address = 10.8.0.2/32
DNS = $dns

[Peer]
PublicKey = $publicKey
Endpoint = $endpoint
AllowedIPs = $allowedIPs
PersistentKeepalive = 25
        """.trimIndent()
    }
    
    fun getSampleConfig(): String {
        return generateConfig(
            privateKey = "cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleQ==",
            publicKey = "cHVibGlja2V5cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleQ==",
            endpoint = "vpn.example.com:51820",
            allowedIPs = "0.0.0.0/0, ::/0",
            dns = "10.8.0.1"
        )
    }
}
