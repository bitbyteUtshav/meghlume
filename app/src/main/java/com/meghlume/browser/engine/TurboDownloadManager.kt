package com.meghlume.browser.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Optional multi-connection downloader (not wired into the UI yet; the browser
 * currently uses Android's DownloadManager). Falls back to a single connection
 * when the server does not support byte ranges.
 */
class TurboDownloadManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(
        url: String,
        destination: File,
        threads: Int = 8,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> },
        onDone: (success: Boolean) -> Unit = {}
    ) {
        scope.launch {
            try {
                val head = client.newCall(Request.Builder().url(url).head().build()).execute().use {
                    val len = it.header("Content-Length")?.toLongOrNull() ?: 0L
                    val ranges = it.header("Accept-Ranges").equals("bytes", ignoreCase = true)
                    len to ranges
                }
                val (total, supportsRanges) = head
                val done = AtomicLong(0)

                if (total <= 0 || !supportsRanges || threads <= 1) {
                    singleStream(url, destination, total, done, onProgress)
                } else {
                    RandomAccessFile(destination, "rw").use { it.setLength(total) }
                    val chunk = total / threads
                    (0 until threads).map { i ->
                        async {
                            val start = i * chunk
                            val end = if (i == threads - 1) total - 1 else start + chunk - 1
                            val req = Request.Builder().url(url)
                                .header("Range", "bytes=$start-$end").build()
                            client.newCall(req).execute().use { resp ->
                                val body = resp.body ?: return@use
                                RandomAccessFile(destination, "rw").use { raf ->
                                    raf.seek(start)
                                    val buf = ByteArray(64 * 1024)
                                    body.byteStream().use { input ->
                                        while (true) {
                                            val n = input.read(buf)
                                            if (n == -1) break
                                            raf.write(buf, 0, n)
                                            onProgress(done.addAndGet(n.toLong()), total)
                                        }
                                    }
                                }
                            }
                        }
                    }.awaitAll()
                }
                onDone(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onDone(false)
            }
        }
    }

    private fun singleStream(
        url: String, dest: File, total: Long, done: AtomicLong,
        onProgress: (Long, Long) -> Unit
    ) {
        client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body ?: return
            dest.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                body.byteStream().use { input ->
                    while (true) {
                        val n = input.read(buf)
                        if (n == -1) break
                        out.write(buf, 0, n)
                        onProgress(done.addAndGet(n.toLong()), total)
                    }
                }
            }
        }
    }
}
