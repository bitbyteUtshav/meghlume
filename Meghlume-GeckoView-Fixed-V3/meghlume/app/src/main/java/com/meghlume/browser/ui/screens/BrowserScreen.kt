package com.meghlume.browser.ui.screens

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.meghlume.browser.engine.GeckoSessionManager
import com.meghlume.browser.ui.components.SafariBottomBar
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserScreen(
    sessionManager: GeckoSessionManager,
    onOpenTabSwitcher: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeTab = sessionManager.currentTab
    var isBottomBarCollapsed by remember { mutableStateOf(false) }

    // Intercept hardware Android back gesture
    BackHandler(enabled = activeTab?.canGoBack == true) {
        sessionManager.goBack()
    }

    // Nested scroll listener to collapse/expand the Safari bottom bar like iOS
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -15f) {
                    isBottomBarCollapsed = true
                } else if (available.y > 15f) {
                    isBottomBarCollapsed = false
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            // GeckoView Native Engine AndroidView
            activeTab?.let { tab ->
                key(tab.id) {
                    AndroidView(
                        factory = { ctx ->
                            GeckoView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setSession(tab.session)
                            }
                        },
                        update = { geckoView ->
                            // Ensure the view is bound to the current session
                            if (geckoView.session != tab.session) {
                                geckoView.setSession(tab.session)
                            }
                        },
                        onRelease = { view -> view.releaseSession() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Safari Floating Glassmorphic Bottom Pill Bar
            SafariBottomBar(
                tabState = activeTab,
                tabsCount = sessionManager.tabs.size,
                isCollapsed = isBottomBarCollapsed,
                onNavigateBack = { sessionManager.goBack() },
                onNavigateForward = { sessionManager.goForward() },
                onReloadOrStop = {
                    if (activeTab?.isLoading == true) {
                        sessionManager.stop()
                    } else {
                        sessionManager.reload()
                    }
                },
                onSubmitUrl = { url -> sessionManager.loadUrl(url) },
                onToggleTabSwitcher = onOpenTabSwitcher,
                onOpenMenu = { /* Open overflow bottom sheet */ },
                onOpenQrScanner = onOpenQrScanner,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
