package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

class StudentIssuesDashboardActivity : AppCompatActivity() {

    private lateinit var tvAlertMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_issues_dashboard)

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        tvAlertMessage = findViewById(R.id.tvAlertMessage)

        setupListeners()
        updateDashboardStats()
        setupCharts()
        loadRecentIssues()
        setupCategoryBars()
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnReportTop).setOnClickListener { startActivity(Intent(this, ReportIssueActivity::class.java)) }
        findViewById<View>(R.id.tvViewAllIssues).setOnClickListener { startActivity(Intent(this, AllIssuesActivity::class.java)) }
        findViewById<View>(R.id.btnReportIssueAction).setOnClickListener { startActivity(Intent(this, ReportIssueActivity::class.java)) }
        findViewById<View>(R.id.btnAllIssuesAction).setOnClickListener { startActivity(Intent(this, AllIssuesActivity::class.java)) }
        findViewById<View>(R.id.btnAnalyticsAction).setOnClickListener { startActivity(Intent(this, AnalysisActivity::class.java)) }
    }

    private fun updateDashboardStats() {
        val issues = DataManager.facultyIssues
        
        findViewById<TextView>(R.id.tvTotalIssues).text = issues.size.toString()
        findViewById<TextView>(R.id.tvOpenIssues).text = issues.count { it.status == "Open" }.toString()
        findViewById<TextView>(R.id.tvInProgressIssues).text = issues.count { it.status == "In Progress" }.toString()
        findViewById<TextView>(R.id.tvResolvedIssues).text = issues.count { it.status == "Resolved" }.toString()

        val urgentCount = issues.count { it.priority.lowercase() == "high" || it.priority.lowercase() == "urgent" }
        if (urgentCount > 0) {
            tvAlertMessage.visibility = View.VISIBLE
            tvAlertMessage.text = "*You have $urgentCount high priority student issues"
        } else {
            tvAlertMessage.visibility = View.GONE
        }
    }

    private fun setupCharts() {
        val issues = DataManager.facultyIssues
        findViewById<PieChartView>(R.id.piePriorityChart).data = issues.groupBy { it.priority }.mapValues { it.value.size }
        findViewById<BarChartView>(R.id.barStatusChart).data = issues.groupBy { it.status }.mapValues { it.value.size }
        findViewById<LineChartView>(R.id.lineTrendChart).data = DataManager.issuesTrendData
    }

    private fun setupCategoryBars() {
        val container = findViewById<LinearLayout>(R.id.llCategoryBars)
        container.removeAllViews()

        val categories = listOf(
            Triple("Academic", "#3B82F6", R.drawable.ic_students),
            Triple("Behavioral", "#EF4444", R.drawable.ic_error),
            Triple("Attendance", "#F97316", R.drawable.ic_calendar),
            Triple("Health", "#10B981", R.drawable.ic_check_circle),
            Triple("Financial", "#8B5CF6", R.drawable.ic_lock),
            Triple("Other", "#64748B", R.drawable.ic_settings)
        )

        val issues = DataManager.facultyIssues
        val maxCount = issues.groupBy { it.category }.mapValues { it.value.size }.values.maxOrNull() ?: 1

        categories.forEach { (name, colorStr, iconRes) ->
            val count = issues.count { it.category == name }
            val itemView = layoutInflater.inflate(R.layout.item_faculty_category, container, false)
            
            itemView.findViewById<TextView>(R.id.tvCategoryName).text = name
            itemView.findViewById<TextView>(R.id.tvCount).text = count.toString()
            itemView.findViewById<ImageView>(R.id.ivCategoryIcon).apply {
                setImageResource(iconRes)
                setColorFilter(colorStr.toColorInt())
            }
            
            val progressBar = itemView.findViewById<ProgressBar>(R.id.pbCategory)
            progressBar.max = maxCount
            progressBar.progress = count
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(colorStr.toColorInt())
            
            container.addView(itemView)
        }
    }

    private fun loadRecentIssues() {
        val container = findViewById<LinearLayout>(R.id.llRecentIssuesContainer)
        container.removeAllViews()
        
        val recentIssues = DataManager.facultyIssues.take(4)
        if (recentIssues.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "No recent issues found"
                setTextColor(Color.GRAY)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            })
            return
        }

        recentIssues.forEach { issue ->
            val itemView = layoutInflater.inflate(R.layout.item_all_issue_card, container, false)
            itemView.findViewById<TextView>(R.id.tvIssueTitle).text = issue.title
            itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description
            itemView.findViewById<TextView>(R.id.tvFacultyName).text = issue.studentName
            itemView.findViewById<TextView>(R.id.tvTimeAgo).text = getString(R.string.reported_format, issue.date)
            itemView.findViewById<TextView>(R.id.tvFacultyAvatar).text = issue.studentName.split(" ").filter { it.isNotBlank() }.take(2).map { it[0] }.joinToString("").uppercase()
            
            val statusTv = itemView.findViewById<TextView>(R.id.tvStatusBadge)
            statusTv.text = issue.status.uppercase()
            
            val priorityTv = itemView.findViewById<TextView>(R.id.tvPriorityBadge)
            priorityTv.text = issue.priority.uppercase()
            
            val statusColor = when(issue.status) {
                "Open" -> "#EF4444".toColorInt()
                "In Progress" -> "#F59E0B".toColorInt()
                "Resolved" -> "#10B981".toColorInt()
                else -> "#64748B".toColorInt()
            }
            statusTv.background.setTint(statusColor.adjustAlpha(0.2f))
            statusTv.setTextColor(statusColor)
            
            itemView.findViewById<TextView>(R.id.tvSemester).text = getString(R.string.issue_category_format, issue.category)
            container.addView(itemView)
        }
    }

    private fun Int.adjustAlpha(factor: Float): Int {
        val alpha = (Color.alpha(this) * factor).roundToInt()
        val red = Color.red(this)
        val green = Color.green(this)
        val blue = Color.blue(this)
        return Color.argb(alpha, red, green, blue)
    }

    override fun onResume() {
        super.onResume()
        updateDashboardStats()
    }
}
