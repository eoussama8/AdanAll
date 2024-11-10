package com.example.adan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrayerTimesAdapter(private val prayerTimes: List<PrayerTime>) : RecyclerView.Adapter<PrayerTimesAdapter.PrayerTimeViewHolder>() {

    // ViewHolder class to represent each item
    inner class PrayerTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prayerName: TextView = itemView.findViewById(R.id.prayerName)
        val prayerTime: TextView = itemView.findViewById(R.id.prayerTime)
    }

    // Create new view holder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerTimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prayer_time, parent, false)
        return PrayerTimeViewHolder(view)
    }

    // Bind data to the view holder
    override fun onBindViewHolder(holder: PrayerTimeViewHolder, position: Int) {
        val prayerTime = prayerTimes[position]
        holder.prayerName.text = prayerTime.name
        holder.prayerTime.text = prayerTime.time
    }

    // Return the size of the list
    override fun getItemCount(): Int = prayerTimes.size
}
