package com.easyprog.android.photogallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.easyprog.android.photogallery.api.FlickrFetch
import com.easyprog.android.photogallery.models.GalleryItem

class PhotoGalleryViewModel: ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>> = FlickrFetch().fetchPhotos()

}