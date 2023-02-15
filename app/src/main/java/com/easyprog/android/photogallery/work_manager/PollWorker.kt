package com.easyprog.android.photogallery.work_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.easyprog.android.photogallery.R
import com.easyprog.android.photogallery.activity.PhotoGalleryActivity
import com.easyprog.android.photogallery.api.FlickrFetch
import com.easyprog.android.photogallery.app.NOTIFICATION_CHANNEL_ID
import com.easyprog.android.photogallery.local_storage.QueryPreferences
import com.easyprog.android.photogallery.models.GalleryItem

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {

        val query = QueryPreferences.getStorageQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetch().fetchPhotosRequest().execute().body()?.photos?.galleryItems
        } else {
            FlickrFetch().searchPhotoRequest(query).execute().body()?.photos?.galleryItems
        } ?: emptyList()

        if (items.isEmpty()) return Result.success()

        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.e("WORK_MANAGER", "Старые результаты: $resultId")
        } else {
            Log.e("WORK_MANAGER", "Новые результаты: $resultId")
            QueryPreferences.setLastResultId(context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0, notification)
        }

        return Result.success()
    }
}