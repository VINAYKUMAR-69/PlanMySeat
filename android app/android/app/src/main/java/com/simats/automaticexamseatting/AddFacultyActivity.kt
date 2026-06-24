package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.simats.automaticexamseatting.network.FacultyRequest
import com.simats.automaticexamseatting.network.MessageResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class AddFacultyActivity : AppCompatActivity() {

    private lateinit var etFacultyName: EditText
    private lateinit var etEmployeeId: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var spinnerDepartment: Spinner
    private lateinit var spinnerDesignation: Spinner
    private lateinit var etQualification: EditText
    private lateinit var etSpecialization: EditText
    private lateinit var etExperience: EditText
    private lateinit var etRating: EditText
    private lateinit var tvDateOfJoining: TextView
    private lateinit var rgAvailability: RadioGroup

    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_faculty)

        // Initialize views
        etFacultyName = findViewById(R.id.etFacultyName)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        spinnerDesignation = findViewById(R.id.spinnerDesignation)
        etQualification = findViewById(R.id.etQualification)
        etSpecialization = findViewById(R.id.etSpecialization)
        etExperience = findViewById(R.id.etExperience)
        etRating = findViewById(R.id.etRating)
        tvDateOfJoining = findViewById(R.id.tvDateOfJoining)
        rgAvailability = findViewById(R.id.rgAvailability)

        setupSpinners()

        findViewById<View>(R.id.llBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btnBackToList).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.llDatePicker).setOnClickListener {
            showDatePicker()
        }

        findViewById<View>(R.id.btnReset).setOnClickListener {
            resetForm()
        }

        findViewById<View>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnAddFacultySubmit).setOnClickListener {
            if (validateForm()) {
                saveFaculty()
            }
        }
    }

    private fun setupSpinners() {
        val departments = arrayOf("CSE", "ECE", "EEE", "Mechanical", "Civil", "IT", "Science & Humanities")
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, departments)
        spinnerDepartment.adapter = deptAdapter

        val designations = arrayOf("Professor", "Associate Professor", "Assistant Professor", "Lecturer", "HOD", "Dean")
        val desigAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, designations)
        spinnerDesignation.adapter = desigAdapter
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvDateOfJoining.text = format.format(selectedDate.time)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        if (etFacultyName.text.isEmpty()) {
            etFacultyName.error = "Name is required"
            return false
        }
        if (etEmployeeId.text.isEmpty()) {
            etEmployeeId.error = "Employee ID is required"
            return false
        }
        if (etEmail.text.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (etPhone.text.isEmpty()) {
            etPhone.error = "Phone is required"
            return false
        }
        if (etQualification.text.isEmpty()) {
            etQualification.error = "Qualification is required"
            return false
        }
        if (etSpecialization.text.isEmpty()) {
            etSpecialization.error = "Specialization is required"
            return false
        }
        if (etExperience.text.isEmpty()) {
            etExperience.error = "Experience is required"
            return false
        }
        if (etRating.text.isEmpty()) {
            etRating.error = "Rating is required"
            return false
        }
        if (tvDateOfJoining.text.isEmpty()) {
            Toast.makeText(this, "Please select Date of Joining", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveFaculty() {
        val name = etFacultyName.text.toString()
        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it[0] }
            .joinToString("")
            .uppercase()

        val status = when (rgAvailability.checkedRadioButtonId) {
            R.id.rbAvailable -> "Active"
            R.id.rbAssigned -> "Assigned"
            R.id.rbOnLeave -> "On Leave"
            else -> "Active"
        }

        val ratingValue = etRating.text.toString().toDoubleOrNull() ?: 0.0
        val expValue = etExperience.text.toString().toIntOrNull() ?: 0
        val empId = etEmployeeId.text.toString()
        val dept = spinnerDepartment.selectedItem.toString()
        val desig = spinnerDesignation.selectedItem.toString()
        val phone = etPhone.text.toString()

        val newFaculty = FacultyMember(
            id = DataManager.facultyMembers.size + 1,
            name = name,
            designation = desig,
            department = dept,
            status = status,
            initials = initials,
            score = ratingValue * 20.0,
            roomsAssigned = if (status == "Assigned") 1 else 0,
            sessions = 0,
            email = etEmail.text.toString(),
            phone = phone,
            qualification = etQualification.text.toString(),
            experience = expValue,
            dateOfJoining = tvDateOfJoining.text.toString(),
            specialization = etSpecialization.text.toString(),
            address = etAddress.text.toString()
        )

        // Add to local manager
        DataManager.facultyMembers.add(newFaculty)

        // Sync with Backend
        val facultyRequest = FacultyRequest(
            facultyId = empId,
            name = name,
            designation = desig,
            department = dept,
            phone = phone,
            experience = expValue,
            papers = 0, // Default for now
            rating = ratingValue.toString(),
            status = status
        )

        RetrofitClient.instance.addFaculty(facultyRequest).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddFacultyActivity, "Faculty added and synced successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddFacultyActivity, "Added locally, but failed to sync with server", Toast.LENGTH_SHORT).show()
                }
                finish()
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@AddFacultyActivity, "Added locally, but Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun resetForm() {
        etFacultyName.text.clear()
        etEmployeeId.text.clear()
        etEmail.text.clear()
        etPhone.text.clear()
        etAddress.text.clear()
        etQualification.text.clear()
        etSpecialization.text.clear()
        etExperience.text.clear()
        etRating.text.clear()
        tvDateOfJoining.text = ""
        rgAvailability.check(R.id.rbAvailable)
        spinnerDepartment.setSelection(0)
        spinnerDesignation.setSelection(0)
    }
}
