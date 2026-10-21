package com.meghlume.browser

import android.app.Application
import com.meghlume.browser.engine.GeckoSecurityConfig
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

class MeghlumeApp : Application() {
    lateinit var geckoRuntime: GeckoRuntime
        private set

    override fun onCreate() {
        super.onCreate()

        val settings = GeckoRuntimeSettings.Builder()
            .javaScriptEnabled(true)
            .consoleOutput(BuildConfig.DEBUG)
            .contentBlocking(GeckoSecurityConfig.contentBlocking())
            .build()

        geckoRuntime = GeckoRuntime.create(this, settings)
    }
}
