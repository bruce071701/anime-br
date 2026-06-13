package com.animebr.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * Checks if the user is in Brazil based on:
 * 1. SIM card country (most reliable)
 * 2. Device language/locale
 *
 * If not in Brazil, redirects to YouTube search with anime name.
 */
object RegionChecker {

    private val BR_LOCALES = setOf("pt", "pt_BR")
    private const val BR_COUNTRY_ISO = "br"

    // Backdoor flag: once activated, bypass region check
    @Volatile
    var bypassEnabled: Boolean = false

    /**
     * Returns true if the user appears to be in Brazil.
     * Checks SIM country first, then device locale.
     */
    fun isBrazilianUser(context: Context): Boolean {
        // Backdoor bypass
        if (bypassEnabled) return true

        // Check SIM card country
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountry = telephonyManager?.simCountryIso?.lowercase()
        if (simCountry != null && simCountry.isNotBlank()) {
            return simCountry == BR_COUNTRY_ISO
        }

        // No SIM card - check device locale
        val locale = Locale.getDefault()
        val language = locale.language.lowercase() // "pt"
        val country = locale.country.lowercase()   // "BR"

        return language == "pt" || country == BR_COUNTRY_ISO
    }

    /**
     * Open browser to search for the anime on YouTube.
     */
    fun openYouTubeSearch(context: Context, animeName: String) {
        val query = Uri.encode(animeName)
        val url = "https://www.youtube.com/results?search_query=$query"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Force browser, avoid YouTube app intercepting
            setPackage("com.android.chrome")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Chrome not available, use any browser
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }
}
