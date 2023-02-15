package com.easyprog.android.photogallery.api

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.LruCache
import android.widget.Button
import androidx.lifecycle.*
import com.easyprog.android.photogallery.models.DownloadedImage
import java.util.concurrent.ConcurrentHashMap

private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap, String) -> Unit
) : HandlerThread("ThumbnailDownloader"), DefaultLifecycleObserver {

    private val lruCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 4
        lruCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    var fragmentLifecycle: Lifecycle? = null
        set(value) {
            field = value
            field?.addObserver(this.fragmentLifecycleObserver)
        }

    private val fragmentLifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            start()
            looper
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            fragmentLifecycle?.removeObserver(this)
            quit()
        }
    }

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, DownloadedImage>()
    private val flickerFetch = FlickrFetch()

    override fun quit(): Boolean {
        hasQuit = true
        lruCache.evictAll()
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String, title: String) {
        requestMap[target] = DownloadedImage(title, url)
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target]?.url ?: return
        val title = requestMap[target]?.title ?: return
        val bitmap: Bitmap

        if (lruCache.get(title) != null) {
            bitmap = lruCache.get(title)
        } else {
            bitmap = flickerFetch.fetchPhoto(url)!!
            lruCache.put(title, bitmap)
        }

        responseHandler.post(Runnable {
            if (requestMap[target]?.url != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap, title)
        })
    }
}