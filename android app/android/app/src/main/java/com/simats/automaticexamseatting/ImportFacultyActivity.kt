package com.simats.automaticexamseatting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.automaticexamseatting.network.FacultyRequest
import com.simats.automaticexamseatting.network.MessageResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportFacultyActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvUploadStatus: TextView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            processFacultyCsv(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_import_faculty)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvUploadStatus = findViewById(R.id.tvUploadStatus)
        bottomNavigation.selectedItemId = R.id.nav_seating
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnCsvUpload).setOnClickListener {
            getContent.launch("*/*")
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                R.id.nav_students -> {
                    startActivity(Intent(this, StudentsActivity::class.java))
                    true
                }
                R.id.nav_seating -> true
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun processFacultyCsv(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val headerLine = reader.readLine()
            
            if (headerLine == null || !headerLine.lowercase().contains("name")) {
                Toast.makeText(this, "Invalid CSV format. Use: Name, Designation, Department", Toast.LENGTH_LONG).show()
                return
            }

            var line: String?
            var addedCount = 0
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split(",")
                if (parts.size >= 3) {
                    val name = parts[0].trim()
                    val designation = parts[1].trim()
                    val department = parts[2].trim()
                    
                    val initials = name.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
                    
                    val newFaculty = FacultyMember(
                        id = DataManager.facultyMembers.size + 1,
                        name = name,
                        designation = designation,
                        department = department,
                        status = "Active",
                        initials = initials,
                        score = 0.0
                    )
                    
                    if (DataManager.facultyMembers.none { it.name == name && it.department == department }) {
                        DataManager.facultyMembers.add(newFaculty)
                        
                        // Sync with backend database
                        val facultyId = "FAC${System.currentTimeMillis() % 10000}$addedCount"
                        val request = FacultyRequest(
                            facultyId = facultyId,
                            name = name,
                            designation = designation,
                            department = department,
                            phone = "",
                            experience = 0,
                            papers = 0,
                            rating = "0.0",
                            status = "Active"
                        )
                        
                        RetrofitClient.instance.addFaculty(request).enqueue(object : Callback<MessageResponse> {
                            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {}
                            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {}
                        })
                        
                        addedCount++
                    }
                }
            }
            reader.close()
            tvUploadStatus.text = "Imported $addedCount faculty members from CSV"
            Toast.makeText(this, "Successfully imported $addedCount faculty members!", Toast.LENGTH_SHORT).show()
            
            // Navigate back to dashboard or show results
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
