package com.simats.automaticexamseatting

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeneratedSeatingPlanActivity : AppCompatActivity() {

    private lateinit var llPlanContainer: LinearLayout
    private lateinit var tvSummary: TextView
    private lateinit var tvExamDetailsHeader: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var userEmail: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            sendSeatingNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generated_seating_plan)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "")?.lowercase()?.trim() ?: ""

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
        
        val current = DataManager.currentExam
        if (current != null && current.allocations.isNotEmpty()) {
            displayPlan(current)
        } else {
            generateAndSaveSeatingPlan()
        }
        
        checkNotificationPermission()
    }

    private fun initializeViews() {
        llPlanContainer = findViewById(R.id.llPlanContainer)
        tvSummary = findViewById(R.id.tvSummary)
        tvExamDetailsHeader = findViewById(R.id.tvExamDetailsHeader)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_seating
    }

    private fun setupListeners() {
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

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<MaterialButton>(R.id.btnExportOptions).setOnClickListener {
            val intent = Intent(this, ExportDataActivity::class.java)
            intent.putExtra(ExportDataActivity.EXTRA_EXPORT_TYPE, ExportDataActivity.TYPE_SEATING_PLAN)
            startActivity(intent)
        }
    }

    private fun generateAndSaveSeatingPlan() {
        val roomCountToUse = intent.getIntExtra("ROOM_COUNT", DataManager.rooms.size)
        val examType = intent.getStringExtra("EXAM_TYPE") ?: DataManager.currentExam?.examType ?: "Final Examination"
        val useAll = intent.getBooleanExtra("USE_ALL", false)
        val examDate = DataManager.currentExam?.examDate ?: "Not Set"
        val examTime = DataManager.currentExam?.examTime ?: "Not Set"

        val report = DataManager.generateSeatingPlan(examType, examDate, examTime, roomCountToUse, useAll)

        if (report == null || report.allocations.isEmpty()) {
            tvSummary.text = "No data to generate plan. Please ensure students and rooms are added."
            return
        }

        DataManager.addNotification(userEmail, "Seating Generated", "Seating plan for $examType has been successfully generated.")

        displayPlan(report)
        savePlanToLocalDatabase(report)
        savePlanToBackend(report)
    }

    private fun savePlanToLocalDatabase(report: SeatingReport) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@GeneratedSeatingPlanActivity)
                val entities = report.allocations.map { allocation ->
                    val room = DataManager.rooms.find { it.number == allocation.roomNumber }
                    SeatingReportEntity(
                        studentName = allocation.studentName,
                        regNo = allocation.regNo,
                        branch = allocation.branch,
                        year = allocation.year,
                        seatNo = allocation.seatNo,
                        roomNumber = allocation.roomNumber,
                        building = room?.building ?: allocation.building ?: "Unknown",
                        invigilator = allocation.assignedFaculty ?: room?.assignedFaculty ?: "Not Assigned",
                        subject = report.examType,
                        date = report.examDate,
                        time = report.examTime
                    )
                }
                db.seatingReportDao().deleteAllReports()
                db.seatingReportDao().insertReports(entities)
            } catch (e: Exception) {
                Log.e("GeneratedPlan", "Local save failed: ${e.message}")
            }
        }
    }

    private fun savePlanToBackend(report: SeatingReport) {
        if (userEmail.isEmpty()) return
        
        report.allocations.forEach { allocation ->
            val room = DataManager.rooms.find { it.number == allocation.roomNumber }
            val request = RoomWiseReportRequest(
                userEmail = userEmail,
                studentName = allocation.studentName,
                regNo = allocation.regNo,
                branch = allocation.branch,
                seatNo = allocation.seatNo,
                roomNumber = allocation.roomNumber,
                building = room?.building ?: allocation.building ?: "Unknown",
                invigilator = allocation.assignedFaculty ?: room?.assignedFaculty ?: "Not Assigned",
                subject = report.examType,
                date = report.examDate,
                time = report.examTime
            )
            
            RetrofitClient.instance.addFinalReport(request).enqueue(object : Callback<RoomWiseReportResponse> {
                override fun onResponse(call: Call<RoomWiseReportResponse>, response: Response<RoomWiseReportResponse>) {}
                override fun onFailure(call: Call<RoomWiseReportResponse>, t: Throwable) {}
            })
        }
    }

    private fun displayPlan(report: SeatingReport) {
        tvExamDetailsHeader.text = "${report.examDate} | ${report.examTime}"
        llPlanContainer.removeAllViews()

        val allocationsByRoom = report.allocations.groupBy { it.roomNumber }
        
        allocationsByRoom.forEach { (roomNumber, roomAllocations) ->
            val roomView = layoutInflater.inflate(R.layout.item_room_plan, llPlanContainer, false)
            roomView.findViewById<TextView>(R.id.tvRoomName).text = "Room $roomNumber"
            
            val tableLayout = roomView.findViewById<TableLayout>(R.id.tlSeatingTable)
            tableLayout.removeAllViews()
            
            roomAllocations.forEachIndexed { i, allocation ->
                val row = TableRow(this)
                row.setPadding(8, 12, 8, 12)
                if (i % 2 == 1) row.setBackgroundColor("#F8FAFC".toColorInt())

                row.addView(createTextView("${allocation.seatNo}", 12f))
                row.addView(createTextView(allocation.studentName, 13f))
                row.addView(createTextView(allocation.regNo, 13f))
                row.addView(createTextView(report.examDate, 11f))
                row.addView(createTextView(report.examTime, 11f))

                tableLayout.addView(row)
            }
            llPlanContainer.addView(roomView)
        }

        tvSummary.text = "${report.totalStudents} students allocated across ${report.roomsUsed} rooms"
    }

    private fun createTextView(text: String, size: Float, isMultiLine: Boolean = false): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = size
        tv.setPadding(0, 0, 12, 0)
        if (isMultiLine) tv.setLineSpacing(2f, 1f)
        return tv
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            sendSeatingNotification()
        }
    }

    private fun sendSeatingNotification() {
        val channelId = "seating_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val channel = NotificationChannel(channelId, "Seating", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, GeneratedSeatingPlanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("Seating Plan Ready")
            .setContentText("The seating arrangement has been generated successfully.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }
}
