package com.simats.automaticexamseatting

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.RoomRequest
import com.simats.automaticexamseatting.network.RoomResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRoomActivity : AppCompatActivity() {

    private var editIndex: Int = -1
    private var roomId: Int = -1
    private var loadingDialog: AlertDialog? = null
    private var tvLoadingMessage: TextView? = null
    private var isSyncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_room)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        val etRoomNumber = findViewById<EditText>(R.id.etRoomNumber)
        val etCapacity = findViewById<EditText>(R.id.etCapacity)
        val etBuilding = findViewById<EditText>(R.id.etBuilding)
        val btnAddRoom = findViewById<MaterialButton>(R.id.btnAddRoom)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        editIndex = intent.getIntExtra("EDIT_INDEX", -1)
        if (editIndex != -1 && editIndex < DataManager.rooms.size) {
            val room = DataManager.rooms[editIndex]
            roomId = room.id
            tvTitle.text = "Edit Room"
            btnAddRoom.text = "Update Room"
            etRoomNumber.setText(room.number)
            etCapacity.setText(room.capacity.toString())
            etBuilding.setText(room.building)
        }

        // Synchronize with server immediately
        fetchRoomsFromServer()

        btnAddRoom.setOnClickListener {
            if (isSyncing) {
                Toast.makeText(this, "Syncing data. Please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roomNumber = etRoomNumber.text.toString().trim()
            val capacityStr = etCapacity.text.toString().trim()
            val building = etBuilding.text.toString().trim()

            if (roomNumber.isEmpty() || capacityStr.isEmpty() || building.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val capacity = capacityStr.toIntOrNull() ?: 0
            if (capacity <= 0) {
                Toast.makeText(this, "Capacity must be a positive number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Client-side duplicate check
            val isDuplicate = DataManager.rooms.any { 
                it.number.equals(roomNumber, ignoreCase = true) && (editIndex == -1 || it.id != roomId)
            }
            if (isDuplicate) {
                Toast.makeText(this, "Room $roomNumber already exists!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("user_email", "")?.lowercase()?.trim() ?: ""

            if (userEmail.isEmpty()) {
                Toast.makeText(this, "Session expired. Log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roomRequest = RoomRequest(userEmail, roomNumber, capacity, building)
            if (editIndex != -1 && roomId != -1) updateRoomInApi(roomId, roomRequest, editIndex)
            else addRoomToApi(roomRequest)
        }

        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }
    }

    private fun showLoading(msg: String) {
        if (loadingDialog == null) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
            tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage)
            loadingDialog = AlertDialog.Builder(this).setView(view).setCancelable(false).create()
        }
        tvLoadingMessage?.text = msg
        if (loadingDialog?.isShowing == false) loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun fetchRoomsFromServer() {
        isSyncing = true
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("user_email", null)?.lowercase()?.trim() ?: return
        RetrofitClient.instance.getRooms(email).enqueue(object : Callback<List<com.simats.automaticexamseatting.network.RoomResponse>> {
            override fun onResponse(call: Call<List<com.simats.automaticexamseatting.network.RoomResponse>>, response: Response<List<com.simats.automaticexamseatting.network.RoomResponse>>) {
                isSyncing = false
                if (response.isSuccessful) {
                    val serverRooms = response.body() ?: emptyList()
                    DataManager.rooms.clear()
                    serverRooms.forEach { r -> 
                        DataManager.rooms.add(RoomDataObj(r.roomNumber, r.building, r.capacity, number = r.roomNumber, id = r.id))
                    }
                }
            }
            override fun onFailure(call: Call<List<com.simats.automaticexamseatting.network.RoomResponse>>, t: Throwable) {
                isSyncing = false
            }
        })
    }

    private fun addRoomToApi(request: RoomRequest) {
        showLoading("Connecting to server...")
        RetrofitClient.instance.addRoom(request).enqueue(object : Callback<RoomResponse> {
            override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!
                    DataManager.rooms.add(RoomDataObj(r.roomNumber, r.building, r.capacity, number = r.roomNumber, id = r.id))
                    Toast.makeText(this@AddRoomActivity, "Room Added Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("AddRoom", "Server Error: $errorBody")
                    val msg = when {
                        errorBody.contains("exist", ignoreCase = true) -> "Room number already exists."
                        response.code() == 500 -> "Server Error (500). The room may already exist."
                        else -> "Failed: ${response.message()}"
                    }
                    Toast.makeText(this@AddRoomActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                hideLoading()
                Toast.makeText(this@AddRoomActivity, "Network Error. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRoomInApi(id: Int, request: RoomRequest, index: Int) {
        showLoading("Saving changes...")
        RetrofitClient.instance.updateRoom(id, request).enqueue(object : Callback<RoomResponse> {
            override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!
                    DataManager.rooms[index] = RoomDataObj(r.roomNumber, r.building, r.capacity, number = r.roomNumber, id = r.id)
                    Toast.makeText(this@AddRoomActivity, "Update Successful!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddRoomActivity, "Update Failed.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                hideLoading()
                Toast.makeText(this@AddRoomActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
