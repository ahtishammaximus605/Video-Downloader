package com.vid.videodownloader.interfaces

import com.vid.videodownloader.model.LinkVideoData
import com.vid.videodownloader.model.VideoData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {
    @POST("api/anything")
    fun postData(@Query("URL") Url: String, @Query("KEY") KEY: String): Call<List<VideoData>>
}
interface LinkApiService {
    @GET("api/link")
    fun postData(@Query("URL") Url: String, @Query("debug") KEY: Int =0): Call<LinkVideoData>
}
//interface ApiService {
//    @GET("68441c50-5fcd-4c6d-b2d5-4e424179954a")
//    fun postData(@Query("URL") Url: String, @Query("KEY") KEY: String): Call<List<VideoData>>
//}
//interface LinkApiService {
//    @GET("api/link")
//    fun postData(@Query("URL") Url: String, @Query("debug") KEY: Int =0): Call<LinkVideoData>
//}