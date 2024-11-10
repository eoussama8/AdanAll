package com.example.adan

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Base URL of your API
    private const val BASE_URL = "https://api.aladhan.com/v1/"

    // Initialize Retrofit
    val api: PrayerTimesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrayerTimesApi::class.java)
    }
}
