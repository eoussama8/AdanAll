package com.example.adan

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PrayerTimesApi {

    @GET("timingsByCity")
    fun getPrayerTimesByCity(
        @Query("date") date: String,
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("x7xapikey") apiKey: String
    ): Call<ApiResponse>

    @GET("timings")
    fun getPrayerTimesByCoordinates(
        @Query("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("x7xapikey") apiKey: String
    ): Call<ApiResponse>

    @GET("timings")
    suspend fun getPrayerTimesByCoordinatesSuspend(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Header("apiKey") apiKey: String
    ): Response<PrayerTime>
}
