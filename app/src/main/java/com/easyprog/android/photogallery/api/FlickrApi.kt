package com.easyprog.android.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {
    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=0a2798a97e603233adafc24a218ec6bd" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<PhotoDeserializer>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}