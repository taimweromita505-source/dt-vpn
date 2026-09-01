package com.taimwe.vpn

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CopyOnWriteArraySet

object AdBlocker {
    private const val TAG = "AdBlocker"
    private val blockedDomains = CopyOnWriteArraySet<String>()
    private var isEnabled = true

    private val defaultBlockLists = listOf(
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "https://raw.githubusercontent.com/adaway/adaway.github.io/master/hosts.txt",
        "https://raw.githubusercontent.com/magnolia1ndyz13/YouTubeAdBlock/master/hosts.txt",
        "https://raw.githubusercontent.com/anudeepND/blackhole/master/hosts",
        "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext"
    )

    private val hardcodedDomains = setOf(
        "doubleclick.net", "googleads.g.doubleclick.net", "googleadservices.com",
        "googlesyndication.com", "googletagmanager.com", "googletagservices.com",
        "facebook.com/tr", "connect.facebook.net", "analytics.google.com",
        "ads.twitter.com", "ads-twitter.com", "amazon-adsystem.com",
        "ads.yahoo.com", "adservice.google.com", "pagead2.googlesyndication.com",
        "tpc.googlesyndication.com", "securepubads.g.doubleclick.net",
        "criteo.com", "adsdk.com", "moatads.com", "pubmatic.com",
        "rubiconproject.com", "openx.net", "casalemedia.com",
        "adnxs.com", "adsafeprotected.com", "scorecardresearch.com",
        // YouTube ad domains (ad-specific, not blocking video content)
        "youtubei.ytimg.com", "i.ytimg.com", "youtubei.googleapis.com",
        "manifest.googlevideo.com", "redirector.googlevideo.com",
        "yt3.ggpht.com", "scontent.xx.fbcdn.net", "scontent.fcgk1-1.fna.fbcdn.net",
        "fbcdn.net", "instagram.com", "cdninstagram.com",
        // animexin / anime streaming ad domains
        "anitracker.org", "kagamynotranslator.com", "gogochan.moe",
        "gogoanime.st", "gogoplay123.com", "gogotaku.info",
        "streamtape.to", "streamtape.io", "doodstream.com", "dood.so",
        "vidcloud.co", "vidstreaming.io", "embedgram.com",
        "animekimi.com", "hitraj.com", "trafficforce.com",
        "propellerads.com", "exoclick.net", "popads.net",
        "adsterra.com", "onclickads.com", "onclickjs.com",
        "zogo.com", "a.aliendi.com", "a.alienwarehouse.com",
        // Telegram ad / tracker domains (Telegram Web sponsored messages)
        "t.me/suggested", "t.me/invite", "telegram.org/ads",
        // General DSP/ad tech
        "taboola.com", "outbrain.com", "revcontent.com",
        "sharethis.com", "addthis.com", "addtoany.com",
        "cdn.outplayad.com", "outplayad.com", "plista.com",
        "spotx.tv", "spotxchange.com", "advertising.com",
        "adtech.com", "adtech.advertising.com", "xaxis.com",
        "dt.ads", "ads-api.twitter.com", "ads-api.spotify.com",
        "sentry.io", "mixpanel.com", "segment.io",
        "amplitude.com", "hotjar.com", "clarity.ms"
    )

    fun initialize(context: Context) {
        if (!isEnabled) return
        Log.d(TAG, "Initializing ad blocker...")
        blockedDomains.clear()
        blockedDomains.addAll(hardcodedDomains)
        CoroutineScope(Dispatchers.IO).launch {
            defaultBlockLists.forEach { url ->
                try {
                    fetchBlockList(url)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch block list: $url", e)
                }
            }
            Log.d(TAG, "Ad blocker initialized with ${blockedDomains.size} domains")
        }
    }

    private suspend fun fetchBlockList(urlString: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.lineSequence().forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            val parts = trimmed.split("\\s+".toRegex())
                            if (parts.size >= 2) {
                                val domain = parts[1].lowercase()
                                if (isValidDomain(domain)) {
                                    blockedDomains.add(domain)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching block list: $urlString", e)
            }
        }
    }

    private fun isValidDomain(domain: String): Boolean {
        return domain.contains(".") && 
               !domain.startsWith(".") &&
               domain.length > 3 &&
               !domain.equals("localhost", ignoreCase = true)
    }

    fun isBlocked(domain: String): Boolean {
        if (!isEnabled) return false
        val normalized = domain.lowercase().trim()
        return blockedDomains.any { 
            normalized == it || normalized.endsWith(".$it")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            blockedDomains.clear()
        }
    }

    fun isEnabled(): Boolean = isEnabled

    fun getBlockedCount(): Int = blockedDomains.size
}
