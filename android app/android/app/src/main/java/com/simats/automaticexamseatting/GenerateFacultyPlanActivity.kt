package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.RoomWiseReportRequest
import com.simats.automaticexamseatting.network.RoomWiseReportResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class GenerateFacultyPlanActivity : AppCompatActivity() {

    private lateinit var etSelectExam: EditText
    private lateinit var etExamDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etDistributionMethod: EditText
    private lateinit var tvRoomsNeeded: TextView
    private lateinit var tvFacultiesAvailable: TextView
    private lateinit var tvStatusMessage: TextView
    private lateinit var btnAddFacultyShortcut: Button
    private lateinit var btnContinue: Button
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generate_faculty_plan)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "")?.lowercase()?.trim() ?: ""

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
        loadExamDetails()
        checkFacultyRequirements()
    }

    private fun initializeViews() {
        etSelectExam = findViewById(R.id.etSelectExam)
        etExamDate = findViewById(R.id.etExamDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        etDistributionMethod = findViewById(R.id.etDistributionMethod)
        
        tvRoomsNeeded = findViewById(R.id.tvRoomsNeeded)
        tvFacultiesAvailable = findViewById(R.id.tvFacultiesAvailable)
        tvStatusMessage = findViewById(R.id.tvStatusMessage)
        btnAddFacultyShortcut = findViewById(R.id.btnAddFacultyShortcut)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun loadExamDetails() {
        DataManager.currentExam?.let {
            etSelectExam.setText(it.examType)
            etExamDate.setText(it.examDate)
            
            if (it.examTime.contains("-")) {
                val times = it.examTime.split("-")
                if (times.size == 2) {
                    etStartTime.setText(times[0].trim())
                    etEndTime.setText(times[1].trim())
                }
            } else {
                etStartTime.setText(it.examTime)
            }
        }
    }

    private fun checkFacultyRequirements() {
        val roomsNeeded = DataManager.currentExam?.roomsUsed ?: 0
        val activeFaculty = DataManager.facultyMembers.count { it.status == "Active" || it.status == "Confirmed" }
        
        tvRoomsNeeded.text = roomsNeeded.toString()
        tvFacultiesAvailable.text = activeFaculty.toString()
        
        when {
            roomsNeeded == 0 -> {
                tvStatusMessage.text = "No rooms allocated in seating plan yet."
                tvStatusMessage.setTextColor(Color.parseColor("#EF4444"))
                btnContinue.isEnabled = false
                btnContinue.alpha = 0.5f
            }
            activeFaculty < roomsNeeded -> {
                val deficit = roomsNeeded - activeFaculty
                tvStatusMessage.text = "Deficit: $deficit faculty member(s) needed."
                tvStatusMessage.setTextColor(Color.parseColor("#EF4444"))
                btnAddFacultyShortcut.visibility = View.VISIBLE
                btnContinue.isEnabled = false
                btnContinue.alpha = 0.5f
            }
            activeFaculty > roomsNeeded -> {
                tvStatusMessage.text = "Surplus: ${activeFaculty - roomsNeeded} faculty available."
                tvStatusMessage.setTextColor(Color.parseColor("#F59E0B"))
                btnAddFacultyShortcut.visibility = View.GONE
                btnContinue.isEnabled = true
                btnContinue.alpha = 1.0f
            }
            else -> {
                tvStatusMessage.text = "Faculty count matches room count exactly."
                tvStatusMessage.setTextColor(Color.parseColor("#059669"))
                btnAddFacultyShortcut.visibility = View.GONE
                btnContinue.isEnabled = true
                btnContinue.alpha = 1.0f
            }
        }
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        btnAddFacultyShortcut.setOnClickListener {
            startActivity(Intent(this, AllFacultyActivity::class.java))
        }

        etExamDate.setOnClickListener {
            showDatePicker(etExamDate)
        }

        etStartTime.setOnClickListener {
            showTimePicker(etStartTime)
        }

        etEndTime.setOnClickListener {
            showTimePicker(etEndTime)
        }

        etSelectExam.setOnClickListener {
            val existingExams = DataManager.getUniqueExamTypes()
            if (existingExams.isEmpty()) {
                Toast.makeText(this, "No exams found in system.", Toast.LENGTH_SHORT).show()
            } else {
                val examsArray = existingExams.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Select Exam Type")
                    .setItems(examsArray) { _, which ->
                        etSelectExam.setText(examsArray[which])
                    }
                    .show()
            }
        }

        etDistributionMethod.setOnClickListener {
            val methods = arrayOf("Balanced Distribution", "Random Allocation", "Department Based", "Seniority Based")
            AlertDialog.Builder(this)
                .setTitle("Distribution Method")
                .setItems(methods) { _, which ->
                    etDistributionMethod.setText(methods[which])
                }
                .show()
        }

        btnContinue.setOnClickListener {
            generateFacultyPlan()
        }
    }

    private fun showDatePicker(editText: EditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear))
        }, year, month, day).show()
    }

    private fun showTimePicker(editText: EditText) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val h12 = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d %s", h12, selectedMinute, amPm))
        }, hour, minute, false).show()
    }

    private fun generateFacultyPlan() {
        val examType = etSelectExam.text.toString()
        val examDate = etExamDate.text.toString()
        val startTime = etStartTime.text.toString()
        val endTime = etEndTime.text.toString()

        if (examType.isEmpty() || examDate.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val roomsNeeded = DataManager.currentExam?.roomsUsed ?: 0
        val activeFaculty = DataManager.facultyMembers.count { it.status == "Active" || it.status == "Confirmed" }
        
        if (activeFaculty < roomsNeeded) {
            Toast.makeText(this, "Still need ${roomsNeeded - activeFaculty} more faculty members", Toast.LENGTH_LONG).show()
            return
        }

        DataManager.currentExam?.let {
            val updatedReport = it.copy(
                examType = examType,
                examDate = examDate,
                examTime = if (startTime.isNotEmpty() && endTime.isNotEmpty()) "$startTime - $endTime" else it.examTime
            )
            DataManager.currentExam = updatedReport
        }
        
        DataManager.assignFacultyToRooms()
        saveUpdatedPlan()

        Toast.makeText(this, "Faculty Allocation Plan Generated and Saved Successfully!", Toast.LENGTH_LONG).show()
        
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun saveUpdatedPlan() {
        val report = DataManager.currentExam ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@GenerateFacultyPlanActivity)
            val entities = report.allocations.map { allocation ->
                val room = DataManager.rooms.find { it.number == allocation.roomNumber }
                SeatingReportEntity(
                    studentName = allocation.studentName,
                    regNo = allocation.regNo,
                    branch = allocation.branch,
                    year = allocation.year,
                    seatNo = allocation.seatNo,
                    roomNumber = allocation.roomNumber,
                    building = room?.building ?: "Unknown",
                    invigilator = allocation.assignedFaculty ?: "Not Assigned",
                    subject = report.examType,
                    date = report.examDate,
                    time = report.examTime
                )
            }
            db.seatingReportDao().deleteAllReports()
            db.seatingReportDao().insertReports(entities)
            
            report.allocations.forEach { allocation ->
                val room = DataManager.rooms.find { it.number == allocation.roomNumber }
                val request = RoomWiseReportRequest(
                    userEmail = userEmail,
                    studentName = allocation.studentName,
                    regNo = allocation.regNo,
                    branch = allocation.branch,
                    seatNo = allocation.seatNo,
                    roomNumber = allocation.roomNumber,
                    building = room?.building ?: "Unknown",
                    invigilator = allocation.assignedFaculty ?: "Not Assigned",
                    subject = report.examType,
                    date = report.examDate,
                    time = report.examTime
                )
                
                RetrofitClient.instance.addSeatingPlan(request).enqueue(object : Callback<RoomWiseReportResponse> {
                    override fun onResponse(call: Call<RoomWiseReportResponse>, response: Response<RoomWiseReportResponse>) {}
                    override fun onFailure(call: Call<RoomWiseReportResponse>, t: Throwable) {}
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkFacultyRequirements()
    }
}
