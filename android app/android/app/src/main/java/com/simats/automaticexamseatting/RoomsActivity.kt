package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomsActivity : AppCompatActivity() {

    private lateinit var llRoomList: LinearLayout
    private lateinit var tlRoomDetails: TableLayout
    private lateinit var tvRoomsCount: TextView
    private var roomsList = mutableListOf<RoomDataObj>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rooms)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        llRoomList = findViewById(R.id.llRoomList)
        tlRoomDetails = findViewById(R.id.tlRoomDetails)
        tvRoomsCount = findViewById(R.id.tvRoomsCount)

        setupListeners()
        loadSmallProfilePhoto()
    }

    override fun onResume() {
        super.onResume()
        // Automatically refresh data whenever the user returns to this screen
        fetchRoomsFromServer()
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { 
            MenuHelper.showCustomMenu(this, it) 
        }

        findViewById<MaterialButton>(R.id.btnAddRoom).setOnClickListener {
            startActivity(Intent(this, AddRoomActivity::class.java))
        }

        findViewById<FrameLayout>(R.id.flNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<CardView>(R.id.cvProfile)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    private fun loadSmallProfilePhoto() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val email = sharedPref.getString("user_email", null)?.lowercase() ?: return
        
        val ivProfile = findViewById<ImageView>(R.id.ivProfileIcon)
        val tvInitial = findViewById<TextView>(R.id.tvProfileInitial)

        RetrofitClient.instance.getProfile(email).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    tvInitial?.text = profile.fullName.take(1).uppercase()
                    
                    if (!profile.photo.isNullOrEmpty()) {
                        val fixedPath = profile.photo.replace("\\", "/")
                        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
                        val cleanPath = fixedPath.trimStart('/')
                        val url = "$baseUrl/$cleanPath"
                        
                        ivProfile?.load(url) {
                            transformations(CircleCropTransformation())
                            listener(
                                onSuccess = { _, _ ->
                                    ivProfile.visibility = View.VISIBLE
                                    tvInitial?.visibility = View.GONE
                                },
                                onError = { _, result ->
                                    ivProfile.visibility = View.GONE
                                    tvInitial?.visibility = View.VISIBLE
                                }
                            )
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {}
        })
    }

    private fun fetchRoomsFromServer() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val email = sharedPref.getString("user_email", null)?.lowercase() ?: return

        RetrofitClient.instance.getRooms(email).enqueue(object : Callback<List<RoomResponse>> {
            override fun onResponse(call: Call<List<RoomResponse>>, response: Response<List<RoomResponse>>) {
                if (response.isSuccessful) {
                    val serverRooms = response.body() ?: emptyList()
                    DataManager.rooms.clear()
                    serverRooms.forEach { r ->
                        DataManager.rooms.add(RoomDataObj(r.roomNumber, r.building, r.capacity, number = r.roomNumber, id = r.id))
                    }
                    refreshData()
                } else {
                    refreshData()
                }
            }
            override fun onFailure(call: Call<List<RoomResponse>>, t: Throwable) {
                refreshData()
            }
        })
    }

    private fun refreshData() {
        roomsList.clear()
        roomsList.addAll(DataManager.rooms)
        refreshUI()
    }

    private fun refreshUI() {
        tvRoomsCount.text = "${roomsList.size} rooms available"
        llRoomList.removeAllViews()
        for ((index, room) in roomsList.withIndex()) {
            val roomView = LayoutInflater.from(this).inflate(R.layout.item_room, llRoomList, false)
            roomView.findViewById<TextView>(R.id.tvRoomNumber).text = room.number
            roomView.findViewById<TextView>(R.id.tvBuilding).text = room.building
            roomView.findViewById<TextView>(R.id.tvCapacity).text = "${room.capacity} seats"
            val percentage = if (room.capacity > 0) (room.occupancy * 100 / room.capacity) else 0
            roomView.findViewById<TextView>(R.id.tvOccupancy).text = "${room.occupancy}/${room.capacity} ($percentage%)"
            roomView.findViewById<ProgressBar>(R.id.pbOccupancy).progress = percentage

            roomView.findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
                startActivity(Intent(this, AddRoomActivity::class.java).apply { putExtra("EDIT_INDEX", index) })
            }
            roomView.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener { showDeleteConfirmation(room) }
            llRoomList.addView(roomView)
        }
        
        // Refresh Table View
        if (tlRoomDetails.childCount >= 2) {
            val header = tlRoomDetails.getChildAt(0)
            val divider = tlRoomDetails.getChildAt(1)
            tlRoomDetails.removeAllViews()
            tlRoomDetails.addView(header)
            tlRoomDetails.addView(divider)
            for (room in roomsList) {
                val row = TableRow(this)
                row.setPadding(0, dpToPx(8), 0, dpToPx(8))
                row.addView(createTableTextView(room.number))
                row.addView(createTableTextView(room.building))
                row.addView(createTableTextView(room.capacity.toString()))
                row.addView(createTableTextView(room.occupancy.toString(), true))
                tlRoomDetails.addView(row)
            }
        }
    }

    private fun createTableTextView(text: String, isEnd: Boolean = false): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.setTextColor(ContextCompat.getColor(this, R.color.black))
        tv.textSize = 12f
        if (isEnd) tv.gravity = android.view.Gravity.END
        return tv
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun showDeleteConfirmation(room: RoomDataObj) {
        AlertDialog.Builder(this)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete room ${room.number}?")
            .setPositiveButton("Delete") { _, _ -> deleteRoomFromApi(room) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRoomFromApi(room: RoomDataObj) {
        RetrofitClient.instance.deleteRoom(room.id).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    DataManager.rooms.removeAll { it.id == room.id }
                    refreshData()
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {}
        })
    }
}
