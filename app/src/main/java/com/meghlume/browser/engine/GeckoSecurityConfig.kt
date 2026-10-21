package com.meghlume.browser.engine

import org.mozilla.geckoview.ContentBlocking

/**
 * Tracker / cookie protection using GeckoView's built-in content blocking.
 * (Ad & tracker lists come from Mozilla; this is not a full ad blocker.)
 */
object GeckoSecurityConfig {
    fun contentBlocking(): ContentBlocking.Settings =
        ContentBlocking.Settings.Builder()
            .antiTracking(
                ContentBlocking.AntiTracking.DEFAULT or
                    ContentBlocking.AntiTracking.FINGERPRINTING or
                    ContentBlocking.AntiTracking.CRYPTOMINING
            )
            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
            .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS)
            .build()
}
