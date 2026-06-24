package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ExamHistoryActivity : AppCompatActivity() {

    private lateinit var tvTotalExportsValue: TextView
    private lateinit var tvTotalSizeValue: TextView
    private lateinit var tvPdfFilesValue: TextView
    private lateinit var tvCsvFilesValue: TextView
    private lateinit var tvStorageUsedValue: TextView
    private lateinit var pbStorage: ProgressBar
    private lateinit var tvSeatingPlansCount: TextView
    private lateinit var tvStudentListsCount: TextView
    private lateinit var tvRoomReportsCount: TextView
    private lateinit var tvAnalyticsCount: TextView
    private lateinit var llRecentExportsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exam_history)
        
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupMenu()
        loadSmallProfilePhoto()
    }

    private fun initViews() {
        tvTotalExportsValue = findViewById(R.id.tvTotalExportsValue)
        tvTotalSizeValue = findViewById(R.id.tvTotalSizeValue)
        tvPdfFilesValue = findViewById(R.id.tvPdfFilesValue)
        tvCsvFilesValue = findViewById(R.id.tvCsvFilesValue)
        tvStorageUsedValue = findViewById(R.id.tvStorageUsedValue)
        pbStorage = findViewById(R.id.pbStorage)
        tvSeatingPlansCount = findViewById(R.id.tvSeatingPlansCount)
        tvStudentListsCount = findViewById(R.id.tvStudentListsCount)
        tvRoomReportsCount = findViewById(R.id.tvRoomReportsCount)
        tvAnalyticsCount = findViewById(R.id.tvAnalyticsCount)
        llRecentExportsContainer = findViewById(R.id.llRecentExportsContainer)
    }

    override fun onResume() {
        super.onResume()
        fetchHistoryAnalytics()
        fetchExamHistory()
    }

    private fun setupMenu() {
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }
        
        findViewById<View>(R.id.cvProfile)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
        
        findViewById<View>(R.id.flNotifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
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
                                onError = { _, _ ->
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

    private fun fetchHistoryAnalytics() {
        RetrofitClient.instance.getExamHistoryAnalytics().enqueue(object : Callback<ExamHistoryAnalyticsResponse> {
            override fun onResponse(call: Call<ExamHistoryAnalyticsResponse>, response: Response<ExamHistoryAnalyticsResponse>) {
                if (response.isSuccessful) {
                    val analytics = response.body() ?: return
                    updateAnalyticsUI(analytics)
                }
            }

            override fun onFailure(call: Call<ExamHistoryAnalyticsResponse>, t: Throwable) {
                Log.e("ExamHistory", "Failed to fetch analytics: ${t.message}")
            }
        })
    }

    private fun updateAnalyticsUI(analytics: ExamHistoryAnalyticsResponse) {
        tvTotalExportsValue.text = analytics.totalExports.toString()
        tvTotalSizeValue.text = analytics.totalSize
        tvPdfFilesValue.text = analytics.pdfFiles.toString()
        tvCsvFilesValue.text = analytics.csvExcelFiles.toString()

        tvStorageUsedValue.text = analytics.storageUsed
        // Assume total storage is 1000 MB for progress bar
        try {
            val usedStr = analytics.storageUsed.replace(" MB", "").trim()
            val used = usedStr.toFloatOrNull() ?: 0f
            pbStorage.progress = ((used / 1000f) * 100).toInt()
        } catch (e: Exception) {
            pbStorage.progress = 0
        }

        tvSeatingPlansCount.text = analytics.seatingPlans.toString()
        tvStudentListsCount.text = analytics.studentLists.toString()
        tvRoomReportsCount.text = analytics.roomReports.toString()
        tvAnalyticsCount.text = analytics.analytics.toString()
    }

    private fun fetchExamHistory() {
        RetrofitClient.instance.getExamHistory().enqueue(object : Callback<List<ExamHistoryResponse>> {
            override fun onResponse(call: Call<List<ExamHistoryResponse>>, response: Response<List<ExamHistoryResponse>>) {
                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()
                    updateHistoryListUI(historyList)
                }
            }

            override fun onFailure(call: Call<List<ExamHistoryResponse>>, t: Throwable) {
                Log.e("ExamHistory", "Failed to fetch history: ${t.message}")
            }
        })
    }

    private fun updateHistoryListUI(history: List<ExamHistoryResponse>) {
        llRecentExportsContainer.removeAllViews()

        if (history.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "No export history available"
            emptyView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyView.setPadding(0, 50, 0, 50)
            emptyView.setTextColor(Color.GRAY)
            llRecentExportsContainer.addView(emptyView)
        } else {
            history.forEach { record ->
                val itemView = layoutInflater.inflate(R.layout.item_export_history, llRecentExportsContainer, false)
                
                val fileNameTv = itemView.findViewById<TextView>(R.id.tvExportFileName)
                val fileTypeTv = itemView.findViewById<TextView>(R.id.tvExportFileType)
                val categoryTv = itemView.findViewById<TextView>(R.id.tvExportCategory)
                val iconIv = itemView.findViewById<ImageView>(R.id.ivExportIcon)

                fileNameTv.text = record.fileName
                fileTypeTv.text = String.format(Locale.getDefault(), "%s • %s", record.fileType, record.fileSize)
                categoryTv.text = record.category

                when (record.fileType.uppercase()) {
                    "PDF" -> iconIv.setImageResource(R.drawable.ic_reports)
                    "CSV", "EXCEL", "XLSX" -> iconIv.setImageResource(R.drawable.ic_work)
                    else -> iconIv.setImageResource(R.drawable.ic_launcher_background)
                }

                when (record.category.lowercase()) {
                    "seating plans" -> {
                        categoryTv.setBackgroundResource(R.drawable.bg_pill_blue)
                        categoryTv.setTextColor(Color.parseColor("#2563EB"))
                    }
                    "student lists" -> {
                        categoryTv.setBackgroundResource(R.drawable.bg_pill_purple)
                        categoryTv.setTextColor(Color.parseColor("#7C3AED"))
                    }
                    "room reports" -> {
                        categoryTv.setBackgroundResource(R.drawable.bg_pill_green)
                        categoryTv.setTextColor(Color.parseColor("#10B981"))
                    }
                    else -> {
                        categoryTv.setBackgroundResource(R.drawable.bg_pill_orange)
                        categoryTv.setTextColor(Color.parseColor("#F59E0B"))
                    }
                }

                itemView.setOnClickListener {
                    Toast.makeText(this, "Opening ${record.fileName}...", Toast.LENGTH_SHORT).show()
                }

                itemView.setOnLongClickListener {
                    deleteHistoryItem(record.id)
                    true
                }
                
                llRecentExportsContainer.addView(itemView)
            }
        }
    }

    private fun deleteHistoryItem(id: Int) {
        RetrofitClient.instance.deleteExamHistory(id).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ExamHistoryActivity, "History deleted", Toast.LENGTH_SHORT).show()
                    onResume() // Refresh data
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@ExamHistoryActivity, "Delete failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
