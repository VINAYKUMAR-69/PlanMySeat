package com.simats.automaticexamseatting

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simats.automaticexamseatting.network.*
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvTotalStudentsHome: TextView
    private lateinit var tvTotalRoomsHome: TextView
    private lateinit var tvTotalAllocatedHome: TextView
    private lateinit var tvTotalBranchesHome: TextView
    private lateinit var tvNotificationCount: TextView
    private lateinit var tvUpcomingExamName: TextView
    private lateinit var tvUpcomingExamDate: TextView
    private lateinit var tvUpcomingExamTime: TextView
    private lateinit var barChart: BarChartView
    private lateinit var pieChart: PieChartView
    private lateinit var fabAiOptimization: FloatingActionButton
    private var userEmail: String = ""
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "")?.lowercase()?.trim() ?: ""
        userRole = sharedPref.getString("user_role", "")?.lowercase()?.trim() ?: ""

        if (userRole == "faculty" || intent.getStringExtra("role")?.lowercase() == "faculty") {
            startActivity(Intent(this, FacultyDashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        findViewById<View>(R.id.main)?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }

        initializeViews()
        setupListeners()
        startAiButtonAnimation()
    }

    override fun onResume() {
        super.onResume()
        refreshDashboardData()
    }

    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        
        tvTotalStudentsHome = findViewById(R.id.tvTotalStudentsHome)
        tvTotalRoomsHome = findViewById(R.id.tvTotalRoomsHome)
        tvTotalAllocatedHome = findViewById(R.id.tvTotalAllocatedHome)
        tvTotalBranchesHome = findViewById(R.id.tvTotalBranchesHome)
        
        tvNotificationCount = findViewById(R.id.tvNotificationCount)
        tvUpcomingExamName = findViewById(R.id.tvUpcomingExamName)
        tvUpcomingExamDate = findViewById(R.id.tvUpcomingExamDate)
        tvUpcomingExamTime = findViewById(R.id.tvUpcomingExamTime)
        
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        fabAiOptimization = findViewById(R.id.fabAiOptimization)
        
        findViewById<TextView>(R.id.tvCurrentDate).text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    private fun startAiButtonAnimation() {
        val rotate = ObjectAnimator.ofFloat(fabAiOptimization, View.ROTATION, 0f, 360f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        
        val scaleX = ObjectAnimator.ofFloat(fabAiOptimization, View.SCALE_X, 1f, 1.15f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        
        val scaleY = ObjectAnimator.ofFloat(fabAiOptimization, View.SCALE_Y, 1f, 1.15f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        AnimatorSet().apply {
            playTogether(rotate, scaleX, scaleY)
            start()
        }
    }

    private fun setupListeners() {
        findViewById<CardView>(R.id.cvLogo).setOnClickListener { showDatePicker() }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); true }
                R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); true }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); true }
                else -> false
            }
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { MenuHelper.showCustomMenu(this, it) }

        findViewById<CardView>(R.id.cvNotificationsBanner).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<CardView>(R.id.cvUpcomingExam).setOnClickListener {
            if (DataManager.currentExam != null) {
                startActivity(Intent(this, GeneratedSeatingPlanActivity::class.java))
            } else {
                startActivity(Intent(this, SeatAllocationSetupActivity::class.java))
            }
        }

        // Overview Cards Navigation
        findViewById<CardView>(R.id.cvStudentsOverview).setOnClickListener {
            startActivity(Intent(this, StudentsActivity::class.java))
        }
        findViewById<CardView>(R.id.cvRoomsOverview).setOnClickListener {
            startActivity(Intent(this, RoomsActivity::class.java))
        }
        findViewById<CardView>(R.id.cvAllocatedOverview).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<CardView>(R.id.cvBranchesOverview).setOnClickListener {
            startActivity(Intent(this, BulkOperationsActivity::class.java))
        }

        findViewById<CardView>(R.id.btnAddStudentAction).setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        findViewById<CardView>(R.id.btnGenerateSeatsAction).setOnClickListener {
            startActivity(Intent(this, SeatAllocationSetupActivity::class.java))
        }

        fabAiOptimization.setOnClickListener {
            startActivity(Intent(this, AiOptimizationActivity::class.java))
        }

        findViewById<TextView>(R.id.tvViewAllAllocations).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selected = Calendar.getInstance().apply { set(year, month, day) }
            findViewById<TextView>(R.id.tvCurrentDate).text = 
                SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(selected.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun refreshDashboardData() {
        // Backend connection disconnected. Using local DataManager only.
        synchronizeDashboardUI()
        updateRecentAllocationsUI()
    }

    private fun synchronizeDashboardUI() {
        val sCount = DataManager.students.size
        val rCount = DataManager.rooms.size
        val bCount = DataManager.students.map { it.branch }.distinct().size
        
        tvTotalStudentsHome.text = sCount.toString()
        tvTotalRoomsHome.text = rCount.toString()
        tvTotalBranchesHome.text = bCount.toString()
        tvNotificationCount.text = "Notifications"

        // Total allocated from current seating report if available
        val aCount = DataManager.currentExam?.totalStudents ?: 0
        tvTotalAllocatedHome.text = aCount.toString()

        // Upcoming exam from current exam
        DataManager.currentExam?.let {
            tvUpcomingExamName.text = it.examType
            tvUpcomingExamDate.text = it.examDate
            tvUpcomingExamTime.text = it.examTime
        } ?: run {
            tvUpcomingExamName.text = "No Upcoming Exams"
            tvUpcomingExamDate.text = "-"
            tvUpcomingExamTime.text = "-"
        }

        updateCharts()
    }

    private fun updateCharts() {
        val branchCounts = DataManager.students.groupBy { it.branch }.mapValues { it.value.size }
        if (branchCounts.isNotEmpty()) {
            barChart.data = branchCounts
            pieChart.data = branchCounts
        } else {
            barChart.data = emptyMap()
            pieChart.data = emptyMap()
        }
    }

    private fun updateRecentAllocationsUI() {
        val container = findViewById<LinearLayout>(R.id.llRecentAllocations)
        container.removeAllViews()
        
        // Use local data from DataManager.currentExam
        val allocations = DataManager.currentExam?.allocations?.take(5) ?: emptyList()
        
        if (allocations.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "No recent allocations found"
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(0, 40, 0, 40)
            })
            return
        }

        allocations.forEach { allocation ->
            val itemView = layoutInflater.inflate(R.layout.item_recent_allocation, container, false)
            itemView.findViewById<TextView>(R.id.tvFacultyName).text = allocation.studentName
            itemView.findViewById<TextView>(R.id.tvDesignation).text = allocation.regNo
            itemView.findViewById<TextView>(R.id.tvRoom).text = "Room ${allocation.roomNumber}"
            itemView.findViewById<TextView>(R.id.tvTime).text = "Seat ${allocation.seatNo}"
            itemView.findViewById<TextView>(R.id.tvDept).text = "Dept: ${allocation.branch}"
            itemView.findViewById<TextView>(R.id.tvDate).text = allocation.date
            
            val statusBadge = itemView.findViewById<TextView>(R.id.tvStatusBadge)
            statusBadge.text = "Local"
            
            val name = allocation.studentName
            itemView.findViewById<TextView>(R.id.tvInitials).text = if (name.length >= 2) name.take(2).uppercase() else name
            container.addView(itemView)
        }
    }
}
