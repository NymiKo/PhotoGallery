package com.easyprog.android.photogallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.easyprog.android.photogallery.api.FlickrFetch
import com.easyprog.android.photogallery.local_storage.QueryPreferences
import com.easyprog.android.photogallery.models.GalleryItem

class PhotoGalleryViewModel(private val app: Application): AndroidViewModel(app) {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetch = FlickrFetch()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStorageQuery(app)
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                Log.e("SEARCH_TERM", searchTerm)
                flickrFetch.fetchPhotos()
            } else {
                flickrFetch.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStorageQuery(app, query)
        mutableSearchTerm.value = query
    }

}