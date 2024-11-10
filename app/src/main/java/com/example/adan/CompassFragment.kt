package com.example.adan

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.*

class CompassFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvCompass: TextView
    private lateinit var compassImageView: ImageView

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var lastAzimuth: Float = 0f
    private var qiblaBearing: Double = 0.0

    private val makkahLatitude = 21.4225
    private val makkahLongitude = 39.8262

    private var magneticFieldSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_compass, container, false)

        tvCompass = rootView.findViewById(R.id.tvCompass)
        compassImageView = rootView.findViewById(R.id.compassImageView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLocationAndCalculateQibla()
        }

        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI)

        return rootView
    }

    private fun getLocationAndCalculateQibla() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                qiblaBearing = calculateQiblaDirection(currentLatitude, currentLongitude)
                tvCompass.text = "Qibla Bearing: $qiblaBearing"
            } ?: run {
                tvCompass.text = "Unable to get location."
            }
        }.addOnFailureListener { exception ->
            tvCompass.text = "Failed to get location: ${exception.localizedMessage}"
        }
    }

    private fun calculateQiblaDirection(userLat: Double, userLon: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLonRad = Math.toRadians(userLon)
        val makkahLatRad = Math.toRadians(makkahLatitude)
        val makkahLonRad = Math.toRadians(makkahLongitude)

        val deltaLon = makkahLonRad - userLonRad
        val y = sin(deltaLon) * cos(makkahLatRad)
        val x = cos(userLatRad) * sin(makkahLatRad) - sin(userLatRad) * cos(makkahLatRad) * cos(deltaLon)

        val bearing = Math.toDegrees(atan2(y, x))

        return (bearing + 360) % 360
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = event.values[0]
                gravity[1] = event.values[1]
                gravity[2] = event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = event.values[0]
                geomagnetic[1] = event.values[1]
                geomagnetic[2] = event.values[2]
            }

            // Check if both accelerometer and magnetic sensor data are available
            if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
                val rotationMatrix = FloatArray(9)
                val orientationValues = FloatArray(3)

                val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)

                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    val bearingDiff = azimuth - qiblaBearing.toFloat()

                    tvCompass.text = "Azimuth: $azimuth\nQibla Bearing: $qiblaBearing\nDifference: $bearingDiff"
                    compassImageView.rotation = bearingDiff
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndCalculateQibla()
            } else {
                tvCompass.text = "Location permission denied. Cannot fetch Qibla direction."
            }
        }
    }
}
