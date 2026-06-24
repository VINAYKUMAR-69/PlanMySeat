package com.simats.automaticexamseatting

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RoomAvailabilityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_room_status_list)

        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvTitle).text = getString(R.string.available_rooms)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.llStatusContainer)
        val availableRooms = DataManager.rooms.filter { it.occupancy < it.capacity }

        if (availableRooms.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = getString(R.string.no_available_rooms_found)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 0)
            }
            container.addView(emptyTv)
        } else {
            for (room in availableRooms) {
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_room_status, container, false)
                itemView.findViewById<TextView>(R.id.tvRoomInfo).text = "Room ${room.number} - ${room.building}"
                itemView.findViewById<TextView>(R.id.tvCapacityInfo).text = "Available: ${room.capacity - room.occupancy} / Total: ${room.capacity}"
                itemView.findViewById<View>(R.id.vStatusIndicator).setBackgroundColor(Color.parseColor("#10B981")) // Green
                container.addView(itemView)
            }
        }
    }
}