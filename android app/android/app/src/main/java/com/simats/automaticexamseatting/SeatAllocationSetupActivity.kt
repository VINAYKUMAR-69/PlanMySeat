package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.RoomResponse
import com.simats.automaticexamseatting.network.StudentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class SeatAllocationSetupActivity : AppCompatActivity() {

    private lateinit var tvRoomCount: TextView
    private lateinit var etExamType: EditText
    private lateinit var etExamDate: EditText
    private lateinit var etExamTime: EditText
    private lateinit var llSelectedRoomsDetails: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView
    
    private var currentRoomSelection = 1
    private var maxRooms = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seat_allocation_setup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerCard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
        fetchInitialData()
    }

    private fun fetchInitialData() {
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
                    updateAnalytics()
                }
            }
            override fun onFailure(call: Call<List<RoomResponse>>, t: Throwable) {
                Toast.makeText(this@SeatAllocationSetupActivity, "Error fetching rooms", Toast.LENGTH_SHORT).show()
                updateAnalytics()
            }
        })

        RetrofitClient.instance.getStudents(email).enqueue(object : Callback<List<StudentResponse>> {
            override fun onResponse(call: Call<List<StudentResponse>>, response: Response<List<StudentResponse>>) {
                if (response.isSuccessful) {
                    val serverStudents = response.body() ?: emptyList()
                    DataManager.students.clear()
                    serverStudents.forEach { s ->
                        val examType = s.examType ?: "Model"
                        val yearInt = s.year?.toIntOrNull() ?: 1
                        DataManager.students.add(Student(s.name ?: "", s.regNo ?: "", s.branch ?: "", yearInt, examType))
                    }
                    updateAnalytics()
                }
            }
            override fun onFailure(call: Call<List<StudentResponse>>, t: Throwable) {
                Toast.makeText(this@SeatAllocationSetupActivity, "Error fetching students", Toast.LENGTH_SHORT).show()
                updateAnalytics()
            }
        })
    }

    private fun updateAnalytics() {
        val studentsCount = DataManager.students.size
        maxRooms = DataManager.rooms.size
        findViewById<TextView>(R.id.tvTotalStudentsAnalytic).text = studentsCount.toString()
        findViewById<TextView>(R.id.tvTotalRoomsAnalytic).text = maxRooms.toString()
        
        // Auto-calculate required rooms based on student count
        if (studentsCount > 0 && maxRooms > 0) {
            var capacityNeeded = studentsCount
            var roomsNeeded = 0
            for (room in DataManager.rooms) {
                if (capacityNeeded <= 0) break
                capacityNeeded -= room.capacity
                roomsNeeded++
            }
            currentRoomSelection = roomsNeeded.coerceIn(1, maxRooms)
        } else {
            if (currentRoomSelection > maxRooms) currentRoomSelection = if (maxRooms > 0) 1 else 0
            if (currentRoomSelection == 0 && maxRooms > 0) currentRoomSelection = 1
        }
        
        updateRoomDisplay()
    }

    private fun initializeViews() {
        tvRoomCount = findViewById(R.id.tvRoomCount)
        etExamType = findViewById(R.id.etExamType)
        etExamDate = findViewById(R.id.etExamDate)
        etExamTime = findViewById(R.id.etExamTime)
        llSelectedRoomsDetails = findViewById(R.id.llSelectedRoomsDetails)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        bottomNavigation.selectedItemId = R.id.nav_seating
        
        currentRoomSelection = if (DataManager.rooms.isNotEmpty()) 1 else 0
        updateRoomDisplay()
    }

    private fun setupListeners() {
        etExamType.setOnLongClickListener {
            val existingExams = DataManager.getUniqueExamTypes()
            if (existingExams.isNotEmpty()) {
                val examsArray = existingExams.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Previous Exam Types")
                    .setItems(examsArray) { _, which ->
                        etExamType.setText(examsArray[which])
                    }
                    .show()
                true
            } else false
        }

        etExamDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this, { _, year, month, day ->
                etExamDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        etExamTime.setOnClickListener {
            val c = Calendar.getInstance()
            val tpd = TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                etExamTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, amPm))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false)
            tpd.show()
        }

        findViewById<CardView>(R.id.cvStudentsAnalytic).setOnClickListener {
            startActivity(Intent(this, StudentsActivity::class.java))
        }
        findViewById<CardView>(R.id.cvRoomsAnalytic).setOnClickListener {
            startActivity(Intent(this, RoomsActivity::class.java))
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); true }
                R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); true }
                R.id.nav_seating -> true
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); true }
                else -> false
            }
        }

        findViewById<Button>(R.id.btnMinus).setOnClickListener {
            if (currentRoomSelection > 1) { currentRoomSelection--; updateRoomDisplay() }
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener {
            if (currentRoomSelection < maxRooms) { currentRoomSelection++; updateRoomDisplay() }
            else Toast.makeText(this, "No more rooms available", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnGenerateSeatingMain).setOnClickListener {
            val examType = etExamType.text.toString().trim()
            if (examType.isEmpty()) {
                Toast.makeText(this, "Please enter Exam Type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalStudentsForExam = DataManager.students.count { it.examType.equals(examType, ignoreCase = true) }
            
            if (totalStudentsForExam == 0) {
                val totalOverall = DataManager.students.size
                if (totalOverall > 0) {
                    AlertDialog.Builder(this)
                        .setTitle("No Students for '$examType'")
                        .setMessage("There are no students specifically assigned to '$examType'. Would you like to use all $totalOverall available students for this plan?")
                        .setPositiveButton("Use All Students") { _, _ ->
                            proceedWithGeneration(examType, totalOverall, true)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    proceedWithGeneration(examType, 0, false)
                }
            } else {
                proceedWithGeneration(examType, totalStudentsForExam, false)
            }
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { 
            MenuHelper.showCustomMenu(this, it)
        }
    }

    private fun proceedWithGeneration(examType: String, studentCount: Int, useAll: Boolean) {
        val selectedRooms = DataManager.rooms.take(currentRoomSelection)
        val totalCapacity = selectedRooms.sumOf { it.capacity }

        val actualCount = if (studentCount == 0 && DataManager.students.isEmpty()) selectedRooms.size * 20 else studentCount

        if (totalCapacity < actualCount) {
            AlertDialog.Builder(this)
                .setTitle("Insufficient Capacity")
                .setMessage("Selected rooms capacity: $totalCapacity\nStudents to allocate: $actualCount\n\nPlease select more rooms or reduce student count.")
                .setPositiveButton("OK", null).show()
        } else {
            DataManager.currentExam = SeatingReport(
                examType = examType,
                examDate = etExamDate.text.toString().ifEmpty { "Not Set" },
                examTime = etExamTime.text.toString().ifEmpty { "Not Set" },
                roomsUsed = currentRoomSelection,
                totalStudents = actualCount,
                allocations = emptyList()
            )
            val intent = Intent(this, GeneratedSeatingPlanActivity::class.java)
            intent.putExtra("ROOM_COUNT", currentRoomSelection)
            intent.putExtra("EXAM_TYPE", examType)
            intent.putExtra("USE_ALL", useAll)
            startActivity(intent)
        }
    }

    private fun updateRoomDisplay() {
        tvRoomCount.text = currentRoomSelection.toString()
        findViewById<TextView>(R.id.tvOfRoomsAvailable).text = "of $maxRooms available"
        llSelectedRoomsDetails.removeAllViews()
        DataManager.rooms.take(currentRoomSelection).forEach { room ->
            val tv = TextView(this)
            tv.text = "• Room ${room.number} | Capacity: ${room.capacity}"
            tv.setTextColor(Color.parseColor("#475569"))
            tv.textSize = 13f
            tv.setPadding(0, 8, 0, 8)
            llSelectedRoomsDetails.addView(tv)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchInitialData()
    }
}
