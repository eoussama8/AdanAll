package com.example.adan

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PrayerTimesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PrayerTimesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var testNotificationButton: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CHANNEL_ID = "prayerTimeChannel"
        private lateinit var goToCompassButton: Button

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goToCompassButton = findViewById(R.id.goToCompassButton)

        // Set up the button click listener to navigate to the CompassActivity
        goToCompassButton.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
        }
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)
        testNotificationButton = findViewById(R.id.testNotificationButton)

        // Initialize ViewModel and Location Client
        viewModel = ViewModelProvider(this).get(PrayerTimesViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, get location
            getLocationAndFetchPrayerTimes()
        }

        // Observe LiveData for prayer times, error, and loading status
        viewModel.prayerTimes.observe(this, { prayerTimes ->
            progressBar.visibility = View.GONE // Hide progress bar once data is loaded
            if (prayerTimes != null) {
                adapter = PrayerTimesAdapter(prayerTimes)
                recyclerView.adapter = adapter
            } else {
                Toast.makeText(this, "No prayer times available", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.error.observe(this, { errorMessage ->
            progressBar.visibility = View.GONE // Hide progress bar on error
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loading.observe(this, { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Set up test notification button
        testNotificationButton.setOnClickListener {
            showNotification()
        }

        // Create notification channel if on Android 8.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Times Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getLocationAndFetchPrayerTimes() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Get the current date in the required format (dd-MM-yyyy)
                    val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                    // Use latitude, longitude, and current date to fetch prayer times
                    viewModel.fetchPrayerTimesByLocation(latitude, longitude, currentDate, "b4601b5c51dc359abc67b6d4eb5dc0c5")
                } else {
                    progressBar.visibility = View.GONE // Hide progress bar if no location
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                progressBar.visibility = View.GONE // Hide progress bar on failure
                Toast.makeText(this, "Error fetching location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            progressBar.visibility = View.GONE // Hide progress bar on exception
            Toast.makeText(this, "Permission denied for location access", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions granted, fetch location and prayer times
            getLocationAndFetchPrayerTimes()
        } else {
            // Handle permission denial
            progressBar.visibility = View.GONE // Hide progress bar
            Toast.makeText(this, "Permission denied. Cannot fetch prayer times without location access.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Prayer Time")
            .setContentText("It's time for your prayer!")
            .setSmallIcon(R.drawable.ic_prayer)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}
