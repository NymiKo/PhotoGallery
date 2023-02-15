package com.easyprog.android.photogallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.easyprog.android.photogallery.api.FlickrFetch
import com.easyprog.android.photogallery.models.GalleryItem

class PhotoGalleryViewModel: ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetch = FlickrFetch()
    private val mutableSearchTerm = MutableLiveData<String>()

    init {
        mutableSearchTerm.value = "planets"
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            flickrFetch.searchPhotos(searchTerm)
        }
    }

    fun fetchPhotos(query: String = "") {
        mutableSearchTerm.value = query
    }

}