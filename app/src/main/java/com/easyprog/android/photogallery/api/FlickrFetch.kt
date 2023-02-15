package com.easyprog.android.photogallery.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.easyprog.android.photogallery.models.GalleryItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

class FlickrFetch {

    private val flickrApi: FlickrApi

    init {
        val client = OkHttpClient.Builder().addInterceptor(PhotoInterceptor()).build()
        val gson: Gson = GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer()).create()
        val retrofit: Retrofit =
            Retrofit.Builder().baseUrl("https://api.flickr.com/").addConverterFactory(
                GsonConverterFactory.create(gson)
            )
                .client(client)
                .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    private fun fetchPhotoMetaData(flickrRequest: Call<PhotoDeserializer>): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

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

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetaData(flickrApi.fetchPhotos())
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetaData(flickrApi.searchPhotos(query))
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        return response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
    }
}