package com.easyprog.android.photogallery.api

import com.easyprog.android.photogallery.models.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}