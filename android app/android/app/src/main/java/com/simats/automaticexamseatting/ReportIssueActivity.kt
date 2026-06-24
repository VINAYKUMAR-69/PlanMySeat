package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class ReportIssueActivity : AppCompatActivity() {

    private lateinit var etStudentId: EditText
    private lateinit var etStudentName: EditText
    private lateinit var etClassSemester: EditText
    private lateinit var etIssueTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etReportedBy: EditText
    private lateinit var etAssignTo: EditText
    private lateinit var etDueDate: EditText
    private lateinit var rgCategory: RadioGroup
    private lateinit var rgPriority: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_issue)

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etStudentId = findViewById(R.id.etStudentId)
        etStudentName = findViewById(R.id.etStudentName)
        etClassSemester = findViewById(R.id.etClassSemester)
        etIssueTitle = findViewById(R.id.etIssueTitle)
        etDescription = findViewById(R.id.etDescription)
        etReportedBy = findViewById(R.id.etReportedBy)
        etAssignTo = findViewById(R.id.etAssignTo)
        etDueDate = findViewById(R.id.etDueDate)
        rgCategory = findViewById(R.id.rgCategory)
        rgPriority = findViewById(R.id.rgPriority)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }

        etDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etDueDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btnSubmitIssue).setOnClickListener {
            submitIssue()
        }
    }

    private fun submitIssue() {
        val title = etIssueTitle.text.toString()
        val studentName = etStudentName.text.toString()
        val studentId = etStudentId.text.toString()
        val description = etDescription.text.toString()

        if (title.isEmpty() || studentName.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = rgCategory.checkedRadioButtonId
        val category = when (categoryId) {
            R.id.rbAcademic -> "Academic"
            R.id.rbBehavioral -> "Behavioral"
            R.id.rbAttendance -> "Attendance"
            R.id.rbHealth -> "Health"
            R.id.rbTechnical -> "Technical"
            else -> "Other"
        }

        val priorityId = rgPriority.checkedRadioButtonId
        val priority = when (priorityId) {
            R.id.rbCritical -> "High"
            R.id.rbHigh -> "High"
            R.id.rbMedium -> "Medium"
            R.id.rbLow -> "Low"
            else -> "Medium"
        }

        val newIssue = FacultyIssue(
            id = DataManager.facultyIssues.size + 1,
            title = title,
            studentName = studentName,
            regNo = studentId,
            category = category,
            status = "Open",
            priority = priority,
            date = "Just now",
            description = description
        )

        DataManager.facultyIssues.add(0, newIssue)
        Toast.makeText(this, "Issue submitted successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}