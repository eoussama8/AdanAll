package com.example.adan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CompassActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        // Here you can initialize your CompassFragment or other compass-related code
        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val compassFragment = CompassFragment() // Instantiate the compass fragment
            fragmentTransaction.replace(R.id.fragment_container, compassFragment) // Replace FrameLayout with the fragment
            fragmentTransaction.commit() // Commit the transaction
        }
    }
}
