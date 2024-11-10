package com.example.adan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class PrayerTimeAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayerTimeChannel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Create a notification channel (for Android 8.0 and higher)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Time Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app when the notification is clicked
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Prayer Time")
            .setContentText("It's time for your prayer!")
            .setSmallIcon(R.drawable.ic_prayer)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Opens the app when clicked
            .build()

        // Show the notification
        notificationManager.notify(0, notification)
    }
}
