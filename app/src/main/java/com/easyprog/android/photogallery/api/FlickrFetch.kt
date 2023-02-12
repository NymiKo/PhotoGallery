package com.easyprog.android.photogallery.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.easyprog.android.photogallery.models.GalleryItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FlickrFetch {

    private val flickrApi: FlickrApi

    init {
        val gson: Gson = GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer()).create()
        val retrofit: Retrofit =
            Retrofit.Builder().baseUrl("https://api.flickr.com/").addConverterFactory(
                GsonConverterFactory.create(gson)
            ).build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoDeserializer> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object : Callback<PhotoDeserializer> {
            override fun onResponse(
                call: Call<PhotoDeserializer>,
                response: Response<PhotoDeserializer>
            ) {
                val flickrResponse: PhotoDeserializer? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot { it.url.isBlank() }
                responseLiveData.value = galleryItems
                Log.e("SUCCESS", galleryItems.toString())
            }

            override fun onFailure(call: Call<PhotoDeserializer>, t: Throwable) {
                Log.e("ERROR", "FAILURE: $t")
            }
        })

        return responseLiveData
    }
}