package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SystemLogsActivity : AppCompatActivity() {

    private lateinit var llLogsContainer: LinearLayout
    private lateinit var etSearchLogs: EditText
    private lateinit var tvTotalLogs: TextView
    private lateinit var tvSuccessLogs: TextView
    private lateinit var tvWarningLogs: TextView
    private lateinit var tvErrorLogs: TextView
    private lateinit var tvStatusFilter: TextView
    private lateinit var tvCategoryFilter: TextView
    private lateinit var tvDateFilter: TextView
    private lateinit var btnNewLogs: MaterialButton
    
    private var currentStatusFilter = "All Status"
    private var currentCategoryFilter = "All Categories"
    private var currentDateFilter = "All Dates"
    private var filteredLogs: List<SystemLog> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_logs)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
        // Initial load handles both display and stats
        applyFilters()
    }

    private fun initializeViews() {
        llLogsContainer = findViewById(R.id.llLogsContainer)
        etSearchLogs = findViewById(R.id.etSearchLogs)
        tvTotalLogs = findViewById(R.id.tvTotalLogs)
        tvSuccessLogs = findViewById(R.id.tvSuccessLogs)
        tvWarningLogs = findViewById(R.id.tvWarningLogs)
        tvErrorLogs = findViewById(R.id.tvErrorLogs)
        tvStatusFilter = findViewById(R.id.tvStatusFilter)
        tvCategoryFilter = findViewById(R.id.tvCategoryFilter)
        tvDateFilter = findViewById(R.id.tvDateFilter)
        btnNewLogs = findViewById(R.id.btnNewLogs)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Refreshing logs...", Toast.LENGTH_SHORT).show()
            applyFilters()
        }

        findViewById<View>(R.id.btnExportLogs).setOnClickListener {
            exportLogsToCSV()
        }

        findViewById<View>(R.id.btnStatusFilter).setOnClickListener { showStatusMenu() }
        findViewById<View>(R.id.btnCategoryFilter).setOnClickListener { showCategoryMenu() }
        findViewById<View>(R.id.btnDateFilter).setOnClickListener { showDatePicker() }

        btnNewLogs.setOnClickListener {
            simulateNewLogin()
        }

        etSearchLogs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun simulateNewLogin() {
        Toast.makeText(this, "Simulating a new login event...", Toast.LENGTH_SHORT).show()
        val newLog = SystemLog(
            DataManager.systemLogs.size + 1,
            "User Login",
            "SUCCESS",
            "Authentication",
            "New login detected from mobile device",
            "current.admin@planmyseat.com",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "login"
        )
        DataManager.systemLogs.add(0, newLog)
        applyFilters()
    }

    private fun showStatusMenu() {
        val popup = PopupMenu(this, findViewById(R.id.btnStatusFilter))
        val statuses = arrayOf("All Status", "SUCCESS", "WARNING", "ERROR", "INFO")
        statuses.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            currentStatusFilter = item.title.toString()
            tvStatusFilter.text = currentStatusFilter
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showCategoryMenu() {
        val popup = PopupMenu(this, findViewById(R.id.btnCategoryFilter))
        val categories = arrayOf("All Categories", "Authentication", "Allocation", "Students", "Exams", "Rooms", "Reports", "Settings")
        categories.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            currentCategoryFilter = item.title.toString()
            tvCategoryFilter.text = currentCategoryFilter
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day)
            currentDateFilter = formattedDate
            tvDateFilter.text = currentDateFilter
            applyFilters()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
                currentDateFilter = "All Dates"
                tvDateFilter.text = currentDateFilter
                applyFilters()
            }
            show()
        }
    }

    private fun updateStats(logs: List<SystemLog>) {
        tvTotalLogs.text = logs.size.toString()
        tvSuccessLogs.text = logs.count { it.status == "SUCCESS" }.toString()
        tvWarningLogs.text = logs.count { it.status == "WARNING" }.toString()
        tvErrorLogs.text = logs.count { it.status == "ERROR" }.toString()
    }

    private fun applyFilters() {
        val query = etSearchLogs.text.toString()
        filteredLogs = DataManager.systemLogs.filter { log ->
            val matchesSearch = log.action.contains(query, ignoreCase = true) || 
                               log.description.contains(query, ignoreCase = true) ||
                               log.user.contains(query, ignoreCase = true)
            
            val matchesStatus = currentStatusFilter == "All Status" || log.status == currentStatusFilter
            val matchesCategory = currentCategoryFilter == "All Categories" || log.category == currentCategoryFilter
            val matchesDate = currentDateFilter == "All Dates" || log.timestamp.startsWith(currentDateFilter)
            
            matchesSearch && matchesStatus && matchesCategory && matchesDate
        }
        displayLogs(filteredLogs)
        updateStats(filteredLogs)
    }

    private fun displayLogs(logs: List<SystemLog>) {
        llLogsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (logs.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No matching logs found"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 100, 0, 100)
                setTextColor("#64748B".toColorInt())
                textSize = 16f
            }
            llLogsContainer.addView(emptyTv)
            return
        }

        logs.forEach { log ->
            val itemView = inflater.inflate(R.layout.item_system_log, llLogsContainer, false)
            
            itemView.findViewById<TextView>(R.id.tvAction).text = log.action
            itemView.findViewById<TextView>(R.id.tvDescription).text = log.description
            itemView.findViewById<TextView>(R.id.tvCategory).text = log.category
            itemView.findViewById<TextView>(R.id.tvUserEmail).text = log.user
            itemView.findViewById<TextView>(R.id.tvTimestamp).text = log.timestamp
            
            val tvStatusBadge = itemView.findViewById<TextView>(R.id.tvStatusBadge)
            tvStatusBadge.text = log.status
            
            val ivLogIcon = itemView.findViewById<ImageView>(R.id.ivLogIcon)
            val cvIconBg = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cvIconBg)

            when (log.status) {
                "SUCCESS" -> {
                    val color = "#10B981".toColorInt()
                    val bgColor = "#DCFCE7".toColorInt()
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
                    tvStatusBadge.setTextColor(color)
                    cvIconBg.setCardBackgroundColor(bgColor)
                    ivLogIcon.setImageResource(R.drawable.ic_check_circle)
                    ivLogIcon.imageTintList = ColorStateList.valueOf(color)
                }
                "WARNING" -> {
                    val color = "#F97316".toColorInt()
                    val bgColor = "#FFEDD5".toColorInt()
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
                    tvStatusBadge.setTextColor(color)
                    cvIconBg.setCardBackgroundColor(bgColor)
                    ivLogIcon.setImageResource(R.drawable.ic_error)
                    ivLogIcon.imageTintList = ColorStateList.valueOf(color)
                }
                "ERROR" -> {
                    val color = "#EF4444".toColorInt()
                    val bgColor = "#FEE2E2".toColorInt()
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
                    tvStatusBadge.setTextColor(color)
                    cvIconBg.setCardBackgroundColor(bgColor)
                    ivLogIcon.setImageResource(R.drawable.ic_error)
                    ivLogIcon.imageTintList = ColorStateList.valueOf(color)
                }
                "INFO" -> {
                    val color = "#2563EB".toColorInt()
                    val bgColor = "#DBEAFE".toColorInt()
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
                    tvStatusBadge.setTextColor(color)
                    cvIconBg.setCardBackgroundColor(bgColor)
                    ivLogIcon.setImageResource(R.drawable.ic_history)
                    ivLogIcon.imageTintList = ColorStateList.valueOf(color)
                }
            }

            llLogsContainer.addView(itemView)
        }
    }

    private fun exportLogsToCSV() {
        if (filteredLogs.isEmpty()) {
            Toast.makeText(this, "No logs to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val csvContent = StringBuilder()
            csvContent.append("ID,Action,Status,Category,Description,User,Timestamp\n")
            filteredLogs.forEach { log ->
                csvContent.append("${log.id},\"${log.action}\",${log.status},\"${log.category}\",\"${log.description}\",\"${log.user}\",${log.timestamp}\n")
            }

            val file = File(cacheDir, "system_logs_export_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { it.write(csvContent.toString().toByteArray()) }

            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export Logs"))
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }
}
