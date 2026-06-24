package com.simats.automaticexamseatting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.StudentRequest
import com.simats.automaticexamseatting.network.StudentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvUploadActivity : AppCompatActivity() {

    private lateinit var llUploadArea: LinearLayout
    private lateinit var btnProceed: View
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvUploadMainText: TextView
    private var loadingDialog: AlertDialog? = null
    private var tvLoadingMessage: TextView? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processCsvFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_upload)

        val headerLayout = findViewById<View>(R.id.headerLayout)
        if (headerLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerLayout) { v, insets ->
                val systemBars = insets.getInsets(com.google.android.material.color.MaterialColors.getColor(v, androidx.appcompat.R.attr.colorPrimary))
                v.setPadding(0, 0, 0, 0)
                insets
            }
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        llUploadArea = findViewById(R.id.llUploadArea)
        btnProceed = findViewById(R.id.btnProceedToSeating)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvUploadMainText = findViewById(R.id.tvUploadMainText)
        bottomNavigation.selectedItemId = R.id.nav_students 
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); true }
                R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); true }
                R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); true }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); true }
                else -> false
            }
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        findViewById<View>(R.id.tvDownloadTemplate).setOnClickListener {
            addTemplateStudents()
            Toast.makeText(this, "Sample students added locally!", Toast.LENGTH_LONG).show()
        }

        llUploadArea.setOnClickListener {
            getContent.launch("text/*")
        }

        btnProceed.setOnClickListener {
            startActivity(Intent(this, SeatAllocationSetupActivity::class.java))
        }
    }

    private fun addTemplateStudents() {
        val templateStudents = listOf(
            Student("John Doe", "CS101", "CSE", 2, "Model"),
            Student("Jane Smith", "EC102", "ECE", 3, "Model")
        )
        templateStudents.forEach { templateStudent ->
            if (DataManager.students.none { it.regNo == templateStudent.regNo }) {
                DataManager.students.add(templateStudent)
            }
        }
    }

    private fun showLoading(msg: String) {
        if (loadingDialog == null) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
            tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage)
            loadingDialog = AlertDialog.Builder(this).setView(view).setCancelable(false).create()
        }
        tvLoadingMessage?.text = msg
        if (loadingDialog?.isShowing == false) loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun processCsvFile(uri: Uri) {
        lifecycleScope.launch {
            showLoading("Reading CSV file...")
            try {
                val inputStream = withContext(Dispatchers.IO) { contentResolver.openInputStream(uri) } ?: throw Exception("Cannot open file")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = withContext(Dispatchers.IO) { reader.readLines() }
                reader.close()

                if (lines.isEmpty()) {
                    hideLoading()
                    showFormatErrorDialog("The selected file is empty.")
                    return@launch
                }

                val header = lines[0]
                if (!isValidHeader(header)) {
                    hideLoading()
                    showFormatErrorDialog("Invalid CSV header. Use: Name, RegNo, Branch, Year, ExamType")
                    return@launch
                }

                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userEmail = sharedPref.getString("user_email", "") ?: ""
                val dataLines = lines.drop(1).filter { it.trim().isNotEmpty() }
                
                if (dataLines.isEmpty()) {
                    hideLoading()
                    Toast.makeText(this@CsvUploadActivity, "No data rows found in CSV", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // New fast parallel uploader
                uploadStudentsInParallel(dataLines, userEmail)

            } catch (e: Exception) {
                hideLoading()
                showFormatErrorDialog("Error reading file: ${e.message}")
            }
        }
    }

    private fun uploadStudentsInParallel(lines: List<String>, email: String) {
        lifecycleScope.launch {
            var successCount = 0
            var failCount = 0
            val total = lines.size
            
            // Process in batches of 10 to speed up significantly while respecting server capacity
            val batchSize = 10
            lines.chunked(batchSize).forEachIndexed { batchIndex, batch ->
                val start = batchIndex * batchSize + 1
                val end = (start + batch.size - 1).coerceAtMost(total)
                showLoading("Uploading students $start to $end of $total...")

                val deferreds = batch.map { line ->
                    async(Dispatchers.IO) {
                        try {
                            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                            if (parts.size >= 2) {
                                val name = parts[0]
                                val regNo = parts[1]
                                val branch = if (parts.size > 2) parts[2] else "General"
                                val yearStr = if (parts.size > 3) parts[3].filter { it.isDigit() }.ifEmpty { "1" } else "1"
                                val yearInt = yearStr.toIntOrNull() ?: 1
                                val examType = if (parts.size > 4) parts[4] else "Model"

                                val request = StudentRequest(email, name, regNo, branch, yearInt.toString(), examType)
                                val response = RetrofitClient.instance.addStudent(request).execute()
                                if (response.isSuccessful) {
                                    synchronized(DataManager.students) {
                                        if (DataManager.students.none { it.regNo == regNo }) {
                                            DataManager.students.add(Student(name, regNo, branch, yearInt, examType))
                                        }
                                    }
                                    return@async true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CsvUpload", "Failed to upload student: ${e.message}")
                        }
                        false
                    }
                }
                
                val results = deferreds.awaitAll()
                successCount += results.count { it }
                failCount += results.count { !it }
            }

            hideLoading()
            val message = "Import Complete: $successCount succeeded, $failCount failed."
            Toast.makeText(this@CsvUploadActivity, message, Toast.LENGTH_LONG).show()
            updateUploadUI(if (successCount > 0) "Completed" else "Failed", successCount)
            DataManager.addNotification(email, "Bulk Import", message)
        }
    }

    private fun isValidHeader(header: String): Boolean {
        val h = header.lowercase()
        return h.contains("name") && (h.contains("reg") || h.contains("no"))
    }

    private fun showFormatErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Import Error")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateUploadUI(status: String, count: Int) {
        tvUploadMainText.text = "$status! $count students added successfully."
        btnProceed.visibility = if (count > 0) View.VISIBLE else View.GONE
    }
}
