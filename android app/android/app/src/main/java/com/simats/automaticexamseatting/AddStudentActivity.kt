package com.simats.automaticexamseatting

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.StudentRequest
import com.simats.automaticexamseatting.network.StudentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_student)
        
        val headerCard = findViewById<android.view.View>(R.id.ivMenu).parent.parent as? android.view.View
        headerCard?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }

        // Setup Spinners
        val spinnerBranch = findViewById<Spinner>(R.id.spinnerBranch)
        val branches = arrayOf("Select branch", "CSE", "ECE", "EEE", "Mechanical", "Civil")
        val branchAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBranch.adapter = branchAdapter

        val spinnerYear = findViewById<Spinner>(R.id.spinnerYear)
        val years = arrayOf("Select year", "Year 1", "Year 2", "Year 3", "Year 4")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etRegNo = findViewById<EditText>(R.id.etRegNo)
        val etExamType = findViewById<EditText>(R.id.etExamType)

        // Back button logic
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        // Cancel button logic
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        // Add Student logic
        findViewById<Button>(R.id.btnAddStudent).setOnClickListener {
            val name = etFullName.text.toString().trim()
            val regNo = etRegNo.text.toString().trim()
            val branch = spinnerBranch.selectedItem.toString()
            val yearStr = spinnerYear.selectedItem.toString()
            val examType = etExamType.text.toString().trim().ifEmpty { "Model" }
            
            if (name.isEmpty() || regNo.isEmpty() || branch == "Select branch" || yearStr == "Select year") {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Extract numeric year (e.g., "Year 1" -> 1)
            val yearInt = yearStr.replace("Year ", "").toIntOrNull() ?: 1

            // Get user email from SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("user_email", "") ?: ""

            // Create Request object
            val studentRequest = StudentRequest(
                userEmail = userEmail,
                name = name,
                regNo = regNo,
                branch = branch,
                year = yearInt.toString(),
                examType = examType
            )

            // API call to add student to backend database
            RetrofitClient.instance.addStudent(studentRequest).enqueue(object : Callback<StudentResponse> {
                override fun onResponse(call: Call<StudentResponse>, response: Response<StudentResponse>) {
                    if (response.isSuccessful) {
                        // Success! Update local data manager so it shows in the list immediately
                        val studentId = response.body()?.id ?: 0
                        DataManager.students.add(Student(name, regNo, branch, yearInt, examType, studentId))

                        Toast.makeText(this@AddStudentActivity, "Student added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@AddStudentActivity, "Error: ${errorBody ?: "Failed to add"}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<StudentResponse>, t: Throwable) {
                    Toast.makeText(this@AddStudentActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Theme toggle setup
        val ivSun = findViewById<ImageView>(R.id.ivSun)
        ivSun.setOnClickListener {
            toggleTheme()
        }
    }

    private fun toggleTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
