package com.simats.automaticexamseatting

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class AddNewUserActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvRole: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var etEmployeeId: EditText
    private lateinit var tvJoiningDate: TextView
    private lateinit var rgStatus: RadioGroup
    private lateinit var btnCreateUser: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBackToUsers: MaterialButton

    private var selectedRole = ""
    private var selectedDepartment = ""
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_new_user)

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tvRole = findViewById(R.id.tvRole)
        tvDepartment = findViewById(R.id.tvDepartment)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        tvJoiningDate = findViewById(R.id.tvJoiningDate)
        rgStatus = findViewById(R.id.rgStatus)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        btnReset = findViewById(R.id.btnReset)
        btnCancel = findViewById(R.id.btnCancel)
        btnBackToUsers = findViewById(R.id.btnBackToUsers)
    }

    private fun setupListeners() {
        btnBackToUsers.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnReset.setOnClickListener { resetForm() }

        findViewById<LinearLayout>(R.id.btnSelectRole).setOnClickListener { showRoleMenu() }
        findViewById<LinearLayout>(R.id.btnSelectDepartment).setOnClickListener { showDepartmentMenu() }
        findViewById<LinearLayout>(R.id.btnSelectJoiningDate).setOnClickListener { showDatePicker() }

        btnCreateUser.setOnClickListener { validateAndCreateUser() }
    }

    private fun showRoleMenu() {
        val popup = PopupMenu(this, findViewById(R.id.btnSelectRole))
        val roles = arrayOf("Admin", "Coordinator", "Staff", "Viewer")
        roles.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            selectedRole = item.title.toString()
            tvRole.text = selectedRole
            tvRole.setTextColor(getColor(R.color.black))
            true
        }
        popup.show()
    }

    private fun showDepartmentMenu() {
        val popup = PopupMenu(this, findViewById(R.id.btnSelectDepartment))
        val depts = arrayOf("Administration", "Examination Cell", "Computer Science", "Electrical Engineering", "Mechanical Engineering")
        depts.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            selectedDepartment = item.title.toString()
            tvDepartment.text = selectedDepartment
            tvDepartment.setTextColor(getColor(R.color.black))
            true
        }
        popup.show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
                tvJoiningDate.text = selectedDate
                tvJoiningDate.setTextColor(getColor(R.color.black))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun resetForm() {
        etFirstName.text.clear()
        etLastName.text.clear()
        etEmail.text.clear()
        etPhone.text.clear()
        etPassword.text.clear()
        etConfirmPassword.text.clear()
        etEmployeeId.text.clear()
        tvRole.text = "Select Role"
        tvRole.setTextColor(getColor(R.color.text_grey))
        tvDepartment.text = "Select Department"
        tvDepartment.setTextColor(getColor(R.color.text_grey))
        tvJoiningDate.text = "Select Date"
        tvJoiningDate.setTextColor(getColor(R.color.text_grey))
        rgStatus.check(R.id.rbActive)
        selectedRole = ""
        selectedDepartment = ""
        selectedDate = ""
    }

    private fun validateAndCreateUser() {
        val fName = etFirstName.text.toString().trim()
        val lName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirmPass = etConfirmPassword.text.toString()
        val empId = etEmployeeId.text.toString().trim()

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || phone.isEmpty() || 
            pass.isEmpty() || selectedRole.isEmpty() || selectedDepartment.isEmpty() || empId.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val status = if (rgStatus.checkedRadioButtonId == R.id.rbActive) "active" else "inactive"
        val initials = "${fName.take(1)}${lName.take(1)}".uppercase()
        
        val newUser = AdminUser(
            id = DataManager.systemUsers.size + 1,
            name = "$fName $lName",
            email = email,
            phone = phone,
            role = selectedRole,
            department = selectedDepartment,
            status = status,
            lastActive = "Never",
            initials = initials
        )

        DataManager.systemUsers.add(0, newUser)
        Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}