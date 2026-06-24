package com.simats.automaticexamseatting

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

class RoomOccupiedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_room_status_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvTitle).text = "Occupied Rooms"
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.llStatusContainer)
        val occupiedRooms = DataManager.rooms.filter { it.occupancy > 0 }

        if (occupiedRooms.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No occupied rooms found"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 0)
            }
            container.addView(emptyTv)
        } else {
            for (room in occupiedRooms) {
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_room_status, container, false)
                itemView.findViewById<TextView>(R.id.tvRoomInfo).text = "Room ${room.number} - ${room.building}"
                itemView.findViewById<TextView>(R.id.tvCapacityInfo).text = "Occupied: ${room.occupancy} / Total: ${room.capacity}"
                itemView.findViewById<View>(R.id.vStatusIndicator).setBackgroundColor(android.graphics.Color.parseColor("#EF4444")) // Red
                container.addView(itemView)
            }
        }
    }
}