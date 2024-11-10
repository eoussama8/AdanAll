package com.example.adan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PrayerTimesViewModel : ViewModel() {

    private val _prayerTimes = MutableLiveData<List<PrayerTime>>()
    val prayerTimes: LiveData<List<PrayerTime>> get() = _prayerTimes

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchPrayerTimes(city: String, country: String, date: String, apiKey: String) {
        // Set loading state to true before making the API call
        _loading.value = true
        _error.value = null // Reset any previous errors

        RetrofitInstance.api.getPrayerTimesByCity(
            apiKey = apiKey,    // Pass the API key here
            date = date,
            city = city,
            country = country
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                // Set loading state to false once the API response is received
                _loading.value = false

                if (response.isSuccessful) {
                    val prayerTimesList = response.body()?.data?.timings?.let {
                        listOf(
                            PrayerTime("Fajr", it.Fajr),
                            PrayerTime("Sunrise", it.Sunrise),
                            PrayerTime("Dhuhr", it.Dhuhr),
                            PrayerTime("Asr", it.Asr),
                            PrayerTime("Sunset", it.Sunset),
                            PrayerTime("Maghrib", it.Maghrib),
                            PrayerTime("Isha", it.Isha),
                            PrayerTime("Imsak", it.Imsak),
                            PrayerTime("Midnight", it.Midnight)
                        )
                    }

                    // Update the prayer times LiveData
                    _prayerTimes.value = prayerTimesList
                } else {
                    // Handle unsuccessful response
                    _error.value = "Failed to fetch prayer times. Response code: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Set loading state to false if there's a failure
                _loading.value = false
                // Handle network failure
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }
    fun fetchPrayerTimesByLocation(latitude: Double, longitude: Double, date: String, apiKey: String) {
        _loading.value = true
        _error.value = null

        // Call the API using Retrofit
        RetrofitInstance.api.getPrayerTimesByCoordinates(
            date = date,
            latitude = latitude,
            longitude = longitude,
            apiKey = apiKey
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                _loading.value = false
                if (response.isSuccessful && response.body() != null) {
                    val prayerTimesList = response.body()?.data?.timings?.let { timings ->
                        listOf(
                            PrayerTime("Fajr", timings.Fajr),
                            PrayerTime("Sunrise", timings.Sunrise),
                            PrayerTime("Dhuhr", timings.Dhuhr),
                            PrayerTime("Asr", timings.Asr),
                            PrayerTime("Sunset", timings.Sunset),
                            PrayerTime("Maghrib", timings.Maghrib),
                            PrayerTime("Isha", timings.Isha),
                            PrayerTime("Imsak", timings.Imsak),
                            PrayerTime("Midnight", timings.Midnight)
                        )
                    }
                    _prayerTimes.value = prayerTimesList
                } else {
                    _error.value = "Failed to fetch prayer times. Please try again later."
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                _loading.value = false
                _error.value = "An error occurred: ${t.message}"
            }
        })
    }
}
