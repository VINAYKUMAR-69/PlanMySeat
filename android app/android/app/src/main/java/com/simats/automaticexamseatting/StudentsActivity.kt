package com.simats.automaticexamseatting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentsActivity : AppCompatActivity() {

    private lateinit var llStudentList: LinearLayout
    private lateinit var tvStudentCount: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var cbSelectAll: CheckBox
    private lateinit var btnDeleteSelected: MaterialButton
    
    private val selectedRegNos = mutableSetOf<String>()
    private var isUpdatingCheckboxes = false
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_students)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
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
        fetchStudentsFromServer()
    }

    private fun initializeViews() {
        llStudentList = findViewById(R.id.llStudentList)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_students
        cbSelectAll = findViewById(R.id.cbSelectAll)
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected)
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); true }
                R.id.nav_students -> true
                R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); true }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); true }
                else -> false
            }
        }

        findViewById<MaterialButton>(R.id.btnCsvUpload).setOnClickListener {
            startActivity(Intent(this, CsvUploadActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAddStudent).setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { 
            MenuHelper.showCustomMenu(this, it) 
        }

        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingCheckboxes) return@setOnCheckedChangeListener
            if (isChecked) {
                DataManager.students.forEach { selectedRegNos.add(it.regNo) }
            } else {
                selectedRegNos.clear()
            }
            refreshListCheckboxes()
            updateDeleteButtonVisibility()
        }

        btnDeleteSelected.setOnClickListener { showBulkDeleteConfirmation() }
    }

    private fun fetchStudentsFromServer() {
        if (userEmail.isEmpty()) return

        RetrofitClient.instance.getStudents(userEmail).enqueue(object : Callback<List<StudentResponse>> {
            override fun onResponse(call: Call<List<StudentResponse>>, response: Response<List<StudentResponse>>) {
                if (response.isSuccessful) {
                    val serverStudents = response.body() ?: emptyList()
                    DataManager.students.clear()
                    serverStudents.forEach { s ->
                        val examType = s.examType ?: "Model"
                        val yearInt = s.year?.toIntOrNull() ?: 1
                        DataManager.students.add(Student(s.name ?: "", s.regNo ?: "", s.branch ?: "", yearInt, examType, s.id))
                    }
                    loadStudents()
                }
            }
            override fun onFailure(call: Call<List<StudentResponse>>, t: Throwable) {
                Log.e("StudentsActivity", "Error: ${t.message}")
            }
        })
    }

    private fun loadStudents() {
        llStudentList.removeAllViews()
        val students = DataManager.students
        tvStudentCount.text = "${students.size} students enrolled"

        if (students.isEmpty()) {
            cbSelectAll.visibility = View.GONE
            val emptyTv = TextView(this).apply {
                text = "No students found."
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(0, 100, 0, 0)
            }
            llStudentList.addView(emptyTv)
        } else {
            cbSelectAll.visibility = View.VISIBLE
            students.forEach { student ->
                val itemView = layoutInflater.inflate(R.layout.item_student, llStudentList, false)
                val cb = itemView.findViewById<CheckBox>(R.id.cbSelectStudent)
                cb.isChecked = selectedRegNos.contains(student.regNo)
                
                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (isUpdatingCheckboxes) return@setOnCheckedChangeListener
                    if (isChecked) selectedRegNos.add(student.regNo) else selectedRegNos.remove(student.regNo)
                    updateDeleteButtonVisibility()
                }

                itemView.findViewById<TextView>(R.id.tvStudentName).text = student.name
                itemView.findViewById<TextView>(R.id.tvRegNo).text = student.regNo
                itemView.findViewById<TextView>(R.id.tvBranch).text = student.branch
                itemView.findViewById<TextView>(R.id.tvYear).text = student.year.toString()
                itemView.findViewById<TextView>(R.id.tvExamType).text = student.examType

                itemView.findViewById<ImageView>(R.id.ivEditStudent).setOnClickListener { showEditStudentDialog(student) }
                
                itemView.findViewById<ImageView>(R.id.ivDeleteStudent).setOnClickListener {
                    showDeleteConfirmation(student)
                }
                
                llStudentList.addView(itemView)
            }
        }
        updateDeleteButtonVisibility()
    }

    private fun showDeleteConfirmation(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete ${student.name}?")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.instance.deleteStudent(student.id).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        if (response.isSuccessful) {
                            DataManager.students.remove(student)
                            loadStudents()
                            Toast.makeText(this@StudentsActivity, "Student deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@StudentsActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        Toast.makeText(this@StudentsActivity, "Connection error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshListCheckboxes() {
        isUpdatingCheckboxes = true
        for (i in 0 until llStudentList.childCount) {
            val itemView = llStudentList.getChildAt(i)
            val regNo = itemView.findViewById<TextView>(R.id.tvRegNo)?.text.toString()
            itemView.findViewById<CheckBox>(R.id.cbSelectStudent)?.isChecked = selectedRegNos.contains(regNo)
        }
        isUpdatingCheckboxes = false
    }

    private fun updateDeleteButtonVisibility() {
        btnDeleteSelected.visibility = if (selectedRegNos.isNotEmpty()) View.VISIBLE else View.GONE
        btnDeleteSelected.text = "Delete (${selectedRegNos.size})"
    }

    private fun showBulkDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Bulk Delete")
            .setMessage("Delete ${selectedRegNos.size} students?")
            .setPositiveButton("Delete") { _, _ ->
                val toDelete = selectedRegNos.toList()
                RetrofitClient.instance.deleteBulkStudents(toDelete).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        if (response.isSuccessful) {
                            DataManager.students.removeAll { selectedRegNos.contains(it.regNo) }
                            selectedRegNos.clear()
                            loadStudents()
                            Toast.makeText(this@StudentsActivity, "Students deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@StudentsActivity, "Failed to delete: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        Toast.makeText(this@StudentsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditStudentDialog(student: Student) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_student, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName).apply { setText(student.name) }
        val etRegNo = dialogView.findViewById<EditText>(R.id.etEditRegNo).apply { setText(student.regNo); isEnabled = false }
        val etBranch = dialogView.findViewById<EditText>(R.id.etEditBranch).apply { setText(student.branch) }
        val etYear = dialogView.findViewById<EditText>(R.id.etEditYear).apply { setText(student.year.toString()) }
        val etExamType = dialogView.findViewById<EditText>(R.id.etEditExamType).apply { setText(student.examType) }

        AlertDialog.Builder(this)
            .setTitle("Edit Student")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val request = StudentRequest(
                    userEmail = userEmail,
                    name = etName.text.toString(), 
                    regNo = student.regNo, 
                    branch = etBranch.text.toString(), 
                    year = etYear.text.toString(), 
                    examType = etExamType.text.toString()
                )
                RetrofitClient.instance.updateStudent(student.regNo, request).enqueue(object : Callback<StudentResponse> {
                    override fun onResponse(call: Call<StudentResponse>, response: Response<StudentResponse>) {
                        if (response.isSuccessful) {
                            fetchStudentsFromServer()
                            Toast.makeText(this@StudentsActivity, "Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<StudentResponse>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        fetchStudentsFromServer()
    }
}