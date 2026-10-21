package com.meghlume.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.meghlume.browser.engine.GeckoSessionManager
import com.meghlume.browser.ui.screens.BrowserScreen
import com.meghlume.browser.ui.screens.QrScannerScreen
import com.meghlume.browser.ui.screens.TabSwitcherScreen
import com.meghlume.browser.ui.theme.MeghlumeTheme

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: GeckoSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = GeckoSessionManager(this)
        openLinkFromIntent(intent)

        setContent {
            MeghlumeTheme {
                var isTabSwitcherOpen by remember { mutableStateOf(false) }
                var isQrScannerOpen by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        isQrScannerOpen -> QrScannerScreen(
                            onUrlDetected = { detected ->
                                sessionManager.loadUrl(detected)
                                isQrScannerOpen = false
                            },
                            onClose = { isQrScannerOpen = false }
                        )
                        isTabSwitcherOpen -> TabSwitcherScreen(
                            sessionManager = sessionManager,
                            onSelectTab = { index ->
                                sessionManager.activeTabIndex = index
                                isTabSwitcherOpen = false
                            },
                            onDone = { isTabSwitcherOpen = false }
                        )
                        else -> BrowserScreen(
                            sessionManager = sessionManager,
                            onOpenTabSwitcher = { isTabSwitcherOpen = true },
                            onOpenQrScanner = { isQrScannerOpen = true }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openLinkFromIntent(intent)
    }

    /** Opens links sent from other apps (when Meghlume is the default browser). */
    private fun openLinkFromIntent(intent: Intent?) {
        val url = intent?.takeIf { it.action == Intent.ACTION_VIEW }?.dataString ?: return
        sessionManager.createNewTab(url)
    }
}
