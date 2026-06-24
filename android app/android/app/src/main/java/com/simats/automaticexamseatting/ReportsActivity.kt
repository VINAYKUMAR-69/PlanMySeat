package com.simats.automaticexamseatting

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.automaticexamseatting.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.OutputStream

class ReportsActivity : AppCompatActivity() {

    private lateinit var rvReportStudents: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var llNoAllocations: LinearLayout
    private lateinit var pbLoading: ProgressBar
    private var reportData: MutableList<RoomWiseReportResponse> = mutableListOf()
    private lateinit var reportsAdapter: ReportsAdapter
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reports)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val headerCard = findViewById<View>(R.id.headerCard)
        if (headerCard != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }

        initializeViews()
        setupListeners()
        loadData()
    }

    private fun initializeViews() {
        rvReportStudents = findViewById(R.id.rvReportStudents)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        llNoAllocations = findViewById(R.id.llNoAllocations)
        pbLoading = findViewById(R.id.pbLoading)
        bottomNavigation.selectedItemId = R.id.nav_reports

        reportsAdapter = ReportsAdapter(reportData) { actionType ->
            when (actionType) {
                ReportsAdapter.ActionType.PREVIEW -> openPreview()
                ReportsAdapter.ActionType.ROOM_WISE -> startActivity(Intent(this, RoomWiseReportActivity::class.java))
                ReportsAdapter.ActionType.EXCEL -> exportAsExcel()
                ReportsAdapter.ActionType.PDF -> downloadReportFromServer()
                ReportsAdapter.ActionType.DELETE -> showDeleteConfirmation()
            }
        }
        rvReportStudents.layoutManager = LinearLayoutManager(this)
        rvReportStudents.adapter = reportsAdapter
    }

    private fun loadData() {
        pbLoading.isVisible = true
        loadLocalData()
        fetchReportsFromServer()
    }

    private fun openPreview() {
        if (reportData.isNotEmpty()) {
            startActivity(Intent(this, GeneratedSeatingPlanActivity::class.java))
        } else {
            Toast.makeText(this, "No plan to preview. Generate one first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); true }
                R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); true }
                R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); true }
                R.id.nav_reports -> true
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); true }
                else -> false
            }
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { 
            MenuHelper.showCustomMenu(this, it)
        }
    }

    private fun loadLocalData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@ReportsActivity)
                val localReports = db.seatingReportDao().getAllReports()
                
                val mapped = localReports.map { 
                    RoomWiseReportResponse(
                        id = it.id,
                        studentName = it.studentName,
                        regNo = it.regNo,
                        branch = it.branch,
                        seatNo = it.seatNo,
                        roomNumber = it.roomNumber,
                        building = it.building,
                        invigilator = it.invigilator,
                        subject = it.subject,
                        date = it.date,
                        time = it.time
                    )
                }

                withContext(Dispatchers.Main) {
                    if (mapped.isNotEmpty()) {
                        reportData.clear()
                        reportData.addAll(mapped)
                        updateUI()
                        pbLoading.isVisible = false
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsActivity", "Error loading local data", e)
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Reports")
            .setMessage("Are you sure you want to clear all seating allocations? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                deleteReportsFromServer()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteReportsFromServer() {
        pbLoading.isVisible = true
        RetrofitClient.instance.deleteFinalReports(userEmail).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                pbLoading.isVisible = false
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportsActivity, "All reports deleted successfully", Toast.LENGTH_SHORT).show()
                    reportData.clear()
                    updateUI()
                    
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@ReportsActivity).seatingReportDao().deleteAllReports()
                    }
                } else {
                    Toast.makeText(this@ReportsActivity, "Failed to delete reports from server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                pbLoading.isVisible = false
                Toast.makeText(this@ReportsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReportsFromServer() {
        RetrofitClient.instance.getFinalReports(userEmail).enqueue(object : Callback<List<RoomWiseReportResponse>> {
            override fun onResponse(call: Call<List<RoomWiseReportResponse>>, response: Response<List<RoomWiseReportResponse>>) {
                pbLoading.isVisible = false
                if (response.isSuccessful) {
                    val networkData = response.body() ?: listOf()
                    if (networkData.isNotEmpty()) {
                        reportData.clear()
                        reportData.addAll(networkData)
                        updateUI()
                        
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val db = AppDatabase.getDatabase(this@ReportsActivity)
                                val entities = networkData.map { 
                                    SeatingReportEntity(
                                        studentName = it.studentName ?: "",
                                        regNo = it.regNo ?: "",
                                        branch = it.branch ?: "",
                                        year = 0,
                                        seatNo = it.seatNo,
                                        roomNumber = it.roomNumber ?: "",
                                        building = it.building ?: "",
                                        invigilator = it.invigilator ?: "",
                                        subject = it.subject ?: "",
                                        date = it.date ?: "",
                                        time = it.time ?: ""
                                    )
                                }
                                db.seatingReportDao().deleteAllReports()
                                db.seatingReportDao().insertReports(entities)
                            } catch (e: Exception) {
                                Log.e("ReportsActivity", "Error syncing network data to local", e)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<RoomWiseReportResponse>>, t: Throwable) {
                pbLoading.isVisible = false
                if (reportData.isEmpty()) {
                    Log.e("ReportsActivity", "Network error: ${t.message}")
                }
            }
        })
    }

    private fun updateUI() {
        if (reportData.isEmpty()) {
            llNoAllocations.isVisible = true
            rvReportStudents.isVisible = false
            return
        }

        llNoAllocations.isVisible = false
        rvReportStudents.isVisible = true
        
        val totalStudents = reportData.size
        val roomsUsed = reportData.map { it.roomNumber }.distinct().size
        val branches = reportData.map { it.branch }.distinct().size
        val examType = reportData.firstOrNull()?.subject ?: "N/A"

        reportsAdapter.updateData(reportData)
        reportsAdapter.setStats(totalStudents, roomsUsed, branches, examType)
    }

    private fun downloadReportFromServer() {
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show()
        RetrofitClient.instance.downloadReport(userEmail).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val fileName = "Exam_Report_${System.currentTimeMillis()}.pdf"
                    saveFileToDownloads(fileName, "application/pdf") { outputStream ->
                        response.body()?.byteStream()?.use { it.copyTo(outputStream) }
                    }
                } else {
                    Toast.makeText(this@ReportsActivity, "PDF generation failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReportsActivity, "Download Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun exportAsExcel() {
        if (reportData.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }
        val sb = StringBuilder()
        sb.append("Student Name,Reg No,Branch,Seat No,Room,Building,Date,Time,Invigilator\n")

        for (report in reportData) {
            sb.append("${report.studentName},${report.regNo},${report.branch},${report.seatNo},${report.roomNumber},${report.building},${report.date},${report.time},${report.invigilator}\n")
        }

        val fileName = "Exam_Seating_Report_${System.currentTimeMillis()}.csv"
        saveFileToDownloads(fileName, "text/csv") { outputStream ->
            outputStream.write(sb.toString().toByteArray())
        }
    }

    private fun saveFileToDownloads(fileName: String, mimeType: String, writeTask: (OutputStream) -> Unit) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    writeTask(outputStream)
                    Toast.makeText(this, "File saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ReportsActivity", "Error saving file", e)
                Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show()
        }
    }
}
