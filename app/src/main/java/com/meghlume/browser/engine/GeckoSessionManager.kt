package com.meghlume.browser.engine

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.meghlume.browser.MeghlumeApp
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.WebResponse
import java.util.UUID

const val HOME_URL = "https://www.google.com"

/** Every field is Compose state, so the UI updates when a page loads or navigates. */
class TabState(
    val session: GeckoSession,
    val isPrivate: Boolean = false,
    initialUrl: String = "about:blank"
) {
    val id: String = UUID.randomUUID().toString()
    var title by mutableStateOf("New Tab")
    var currentUrl by mutableStateOf(initialUrl)
    var isLoading by mutableStateOf(false)
    var progress by mutableStateOf(0)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var isSecure by mutableStateOf(false)
}

class GeckoSessionManager(private val context: Context) {
    private val runtime = (context.applicationContext as MeghlumeApp).geckoRuntime

    val tabs = mutableStateListOf<TabState>()
    var activeTabIndex by mutableStateOf(0)

    val currentTab: TabState?
        get() = tabs.getOrNull(activeTabIndex)

    init {
        createNewTab(HOME_URL)
    }

    fun createNewTab(initialUrl: String = HOME_URL, isPrivate: Boolean = false): TabState {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(isPrivate)
            .useTrackingProtection(true)
            .suspendMediaWhenInactive(true)
            .build()

        val session = GeckoSession(settings)
        val tab = TabState(session = session, isPrivate = isPrivate, initialUrl = initialUrl)
        setupDelegates(session, tab)

        session.open(runtime)
        if (initialUrl != "about:blank") session.loadUri(initialUrl)

        tabs.add(tab)
        activeTabIndex = tabs.size - 1
        return tab
    }

    private fun setupDelegates(session: GeckoSession, tab: TabState) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                tab.currentUrl = url ?: "about:blank"
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                tab.canGoBack = canGoBack
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                tab.canGoForward = canGoForward
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                tab.isLoading = true
                tab.progress = 5
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                tab.isLoading = false
                tab.progress = 100
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                tab.progress = progress
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) {
                tab.isSecure = securityInfo.isSecure
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) tab.title = title
            }

            // File downloads are handed to Android's system DownloadManager
            override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                startDownload(response.uri)
            }
        }
    }

    private fun startDownload(url: String) {
        try {
            val fileName = URLUtil.guessFileName(url, null, null)
            val request = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeTab(index: Int) {
        if (index !in tabs.indices) return
        if (tabs.size == 1) {
            // Always keep one tab: replace the last one with a fresh tab
            val old = tabs.removeAt(0)
            old.session.close()
            createNewTab(HOME_URL)
            return
        }
        val closing = tabs.removeAt(index)
        closing.session.close()
        activeTabIndex = when {
            index < activeTabIndex -> activeTabIndex - 1
            activeTabIndex >= tabs.size -> tabs.size - 1
            else -> activeTabIndex
        }
    }

    fun loadUrl(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        val formatted = when {
            text.startsWith("http://") || text.startsWith("https://") -> text
            text.contains(".") && !text.contains(" ") -> "https://$text"
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(text, "UTF-8")}"
        }
        currentTab?.session?.loadUri(formatted)
    }

    fun goBack() = currentTab?.session?.goBack()
    fun goForward() = currentTab?.session?.goForward()
    fun reload() = currentTab?.session?.reload()
    fun stop() = currentTab?.session?.stop()
}
