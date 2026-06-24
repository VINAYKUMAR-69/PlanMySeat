package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityMonitorActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChartView
    private lateinit var donutChart: DonutChartView
    private lateinit var browserChart: BarChartView
    private lateinit var llMostVisited: LinearLayout
    private lateinit var llActiveUsersContainer: LinearLayout
    
    // Summary Views
    private lateinit var tvActiveUsersCount: TextView
    private lateinit var tvTotalSessionsCount: TextView
    private lateinit var tvAvgSessionTime: TextView
    private lateinit var tvActionsPerDay: TextView
    private lateinit var tvActiveUsersTableSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activity_monitor)

        val mainView = findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
        populateData()
    }

    private fun initializeViews() {
        lineChart = findViewById(R.id.lineChart)
        donutChart = findViewById(R.id.donutChart)
        browserChart = findViewById(R.id.browserChart)
        llMostVisited = findViewById(R.id.llMostVisited)
        llActiveUsersContainer = findViewById(R.id.llActiveUsersContainer)
        
        tvActiveUsersCount = findViewById(R.id.tvActiveUsersCount)
        tvTotalSessionsCount = findViewById(R.id.tvTotalSessionsCount)
        tvAvgSessionTime = findViewById(R.id.tvAvgSessionTime)
        tvActionsPerDay = findViewById(R.id.tvActionsPerDay)
        tvActiveUsersTableSubtitle = findViewById(R.id.tvActiveUsersTableSubtitle)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show()
            populateData()
        }
        findViewById<View>(R.id.btnExport).setOnClickListener {
            Toast.makeText(this, "Exporting activity report...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateData() {
        // Summary values (Mock data from DataManager or hardcoded based on user requirements)
        tvActiveUsersCount.text = "5"
        tvTotalSessionsCount.text = "103"
        tvAvgSessionTime.text = "24m"
        tvActionsPerDay.text = "425"
        tvActiveUsersTableSubtitle.text = "5 users currently online"

        // Charts
        lineChart.data = DataManager.activityTimeline
        donutChart.data = DataManager.deviceDistribution
        browserChart.data = DataManager.browserDistribution

        // Most Visited Pages
        llMostVisited.removeAllViews()
        val inflater = LayoutInflater.from(this)
        DataManager.mostVisitedPages.forEachIndexed { index, page ->
            val itemView = inflater.inflate(R.layout.item_most_visited_page, llMostVisited, false)
            itemView.findViewById<TextView>(R.id.tvPageName).text = page.page
            itemView.findViewById<TextView>(R.id.tvVisitCount).text = page.visits.toString()
            itemView.findViewById<TextView>(R.id.tvIndex).text = (index + 1).toString()
            
            val progressBar = itemView.findViewById<ProgressBar>(R.id.pbVisits)
            progressBar.max = 250 // Max visits for scale
            progressBar.progress = page.visits
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(page.color.toColorInt())
            
            llMostVisited.addView(itemView)
        }

        // Active Users Table
        llActiveUsersContainer.removeAllViews()
        DataManager.activeUsersList.forEach { user ->
            val itemView = inflater.inflate(R.layout.item_active_user_row, llActiveUsersContainer, false)
            
            itemView.findViewById<TextView>(R.id.tvUserName).text = user.name
            itemView.findViewById<TextView>(R.id.tvUserInitials).text = user.initials
            itemView.findViewById<TextView>(R.id.tvCurrentPage).text = user.page
            itemView.findViewById<TextView>(R.id.tvDevice).text = user.device
            itemView.findViewById<TextView>(R.id.tvBrowser).text = user.browser
            itemView.findViewById<TextView>(R.id.tvLoginTime).text = user.loginTime
            itemView.findViewById<TextView>(R.id.tvLastSeen).text = user.lastSeen
            itemView.findViewById<TextView>(R.id.tvActionsCount).text = user.actions.toString()

            llActiveUsersContainer.addView(itemView)
        }
    }
}