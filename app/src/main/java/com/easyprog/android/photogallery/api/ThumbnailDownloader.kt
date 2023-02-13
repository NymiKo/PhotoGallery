package com.easyprog.android.photogallery.api

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.*
import com.easyprog.android.photogallery.models.DownloadedImage
import java.util.concurrent.ConcurrentHashMap

private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap, String) -> Unit
    ) : HandlerThread("ThumbnailDownloader"), DefaultLifecycleObserver {

    val fragmentLifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            start()
            looper
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            quit()
        }
    }

    val viewLifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, DownloadedImage>()
    private val flickerFetch = FlickrFetch()

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String, title: String) {
        requestMap[target] = DownloadedImage(title, url)
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
        Log.e("REQUEST_MAP", requestMap[target].toString())
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
        val bitmap = flickerFetch.fetchPhoto(url) ?: return

        responseHandler.post(Runnable {
            if (requestMap[target]?.url != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap, title)
        })
    }
}