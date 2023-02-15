package com.easyprog.android.photogallery.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.easyprog.android.photogallery.api.FlickrFetch
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
        }

        return Result.success()
    }
}