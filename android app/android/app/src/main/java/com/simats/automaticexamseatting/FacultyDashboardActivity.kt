package com.simats.automaticexamseatting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyDashboardActivity : AppCompatActivity() {

    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_faculty_dashboard)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        setupListeners()
        refreshFacultyData()
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<View>(R.id.tvViewAllAllocations).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<View>(R.id.btnAllFaculty).setOnClickListener {
            startActivity(Intent(this, AllFacultyActivity::class.java))
        }
        
        findViewById<View>(R.id.btnHeaderAddPlan).setOnClickListener {
            startActivity(Intent(this, GenerateFacultyPlanActivity::class.java))
        }

        findViewById<View>(R.id.btnGeneratePlan).setOnClickListener {
            startActivity(Intent(this, GenerateFacultyPlanActivity::class.java))
        }

        findViewById<View>(R.id.btnViewSchedule).setOnClickListener {
            startActivity(Intent(this, ExamHistoryActivity::class.java))
        }
    }

    private fun refreshFacultyData() {
        RetrofitClient.instance.getFaculties().enqueue(object : Callback<List<FacultyResponse>> {
            override fun onResponse(call: Call<List<FacultyResponse>>, response: Response<List<FacultyResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val remoteFaculty = response.body()!!
                    updateDashboardWithRemoteData(remoteFaculty)
                    setupCharts(remoteFaculty)
                    loadAllocations(remoteFaculty)
                    loadTopPerformers(remoteFaculty)
                } else {
                    updateDashboardStats()
                }
            }

            override fun onFailure(call: Call<List<FacultyResponse>>, t: Throwable) {
                Log.e("FacultyDashboard", "Error: ${t.message}")
                updateDashboardStats()
            }
        })
    }

    private fun updateDashboardWithRemoteData(faculty: List<FacultyResponse>) {
        val total = faculty.size
        findViewById<TextView>(R.id.tvTotalFaculty).text = total.toString()
        findViewById<TextView>(R.id.tvTotalPercent).text = "Total"
        
        val activeCount = faculty.count { it.status.equals("Active", ignoreCase = true) || it.status.equals("Confirmed", ignoreCase = true) }
        findViewById<TextView>(R.id.tvAvailableFaculty).text = activeCount.toString()
        val availablePercent = if (total > 0) (activeCount * 100 / total) else 0
        findViewById<TextView>(R.id.tvAvailablePercent).text = "$availablePercent%"
        
        // Use actual logic for assigned count if possible, or set to 0 if not available
        val assignedCount = 0 
        findViewById<TextView>(R.id.tvAssignedFaculty).text = assignedCount.toString()
        findViewById<TextView>(R.id.tvAssignedPercent).text = "0%"
        
        findViewById<TextView>(R.id.tvUpcomingDuties).text = "0"
        findViewById<TextView>(R.id.tvUpcomingPercent).text = "Next Week"
    }

    private fun updateDashboardStats() {
        val faculty = DataManager.facultyMembers
        val total = faculty.size
        
        findViewById<TextView>(R.id.tvTotalFaculty).text = total.toString()
        findViewById<TextView>(R.id.tvTotalPercent).text = "Total"
        
        val assignedCount = faculty.count { it.roomsAssigned > 0 }
        findViewById<TextView>(R.id.tvAssignedFaculty).text = assignedCount.toString()
        val assignedPercent = if (total > 0) (assignedCount * 100 / total) else 0
        findViewById<TextView>(R.id.tvAssignedPercent).text = "$assignedPercent%"
        
        val availableCount = faculty.count { (it.status == "Active" || it.status == "Confirmed") && it.roomsAssigned == 0 }
        findViewById<TextView>(R.id.tvAvailableFaculty).text = availableCount.toString()
        val availablePercent = if (total > 0) (availableCount * 100 / total) else 0
        findViewById<TextView>(R.id.tvAvailablePercent).text = "$availablePercent%"
        
        findViewById<TextView>(R.id.tvUpcomingDuties).text = "0"
        findViewById<TextView>(R.id.tvUpcomingPercent).text = "Next Week"
    }

    private fun setupCharts(faculty: List<FacultyResponse>) {
        val deptData = faculty.groupBy { it.department }
            .mapValues { it.value.size }
        if (deptData.isNotEmpty()) {
            findViewById<DonutChartView>(R.id.donutDeptChart).data = deptData
        }

        val statusData = faculty.groupBy { it.status }
            .mapValues { it.value.size }
        if (statusData.isNotEmpty()) {
            findViewById<BarChartView>(R.id.barDutyChart).data = statusData
        }
        
        findViewById<LineChartView>(R.id.lineAssociationChart).data = DataManager.activityTimeline
    }

    private fun loadAllocations(faculty: List<FacultyResponse>) {
        val container = findViewById<LinearLayout>(R.id.llAllocationsContainer)
        container.removeAllViews()
        
        // In a real app, this should load actual session/duty allocations
        // For now, removing mock allocations if no data exists
    }

    private fun loadTopPerformers(faculty: List<FacultyResponse>) {
        val container = findViewById<LinearLayout>(R.id.llPerformersContainer)
        container.removeAllViews()
        
        val topPerformers = faculty.filter { (it.rating.toDoubleOrNull() ?: 0.0) > 0 }
            .sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
            .take(5)
        
        topPerformers.forEachIndexed { index, performer ->
            val itemView = layoutInflater.inflate(R.layout.item_most_visited_page, container, false)
            
            itemView.findViewById<TextView>(R.id.tvIndex).text = (index + 1).toString()
            itemView.findViewById<TextView>(R.id.tvPageName).text = performer.name
            itemView.findViewById<TextView>(R.id.tvVisitCount).text = "${performer.rating}%"
            
            val colors = listOf("#F59E0B", "#3B82F6", "#10B981", "#8B5CF6", "#EC4899")
            itemView.findViewById<View>(R.id.tvIndex).background?.setTint(Color.parseColor(colors[index % colors.size]))
            
            val progressBar = itemView.findViewById<android.widget.ProgressBar>(R.id.pbVisits)
            progressBar.progress = (performer.rating.toDoubleOrNull() ?: 0.0).toInt()
            progressBar.progressTintList = 
                android.content.res.ColorStateList.valueOf(Color.parseColor(colors[index % colors.size]))
            
            container.addView(itemView)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFacultyData()
    }
}
