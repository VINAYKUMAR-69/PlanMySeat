package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var tlColleges: TableLayout
    private lateinit var tlAdmins: TableLayout
    private lateinit var tvCollegesCount: TextView
    private lateinit var tvAdminsCount: TextView
    private lateinit var tvStudentsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_panel)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tlColleges = findViewById(R.id.tlColleges)
        tlAdmins = findViewById(R.id.tlAdmins)
        tvCollegesCount = findViewById(R.id.tvCollegesCount)
        tvAdminsCount = findViewById(R.id.tvAdminsCount)
        tvStudentsCount = findViewById(R.id.tvStudentsCount)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<MaterialButton>(R.id.btnAddCollege).setOnClickListener {
            showAddCollegeDialog()
        }

        findViewById<MaterialButton>(R.id.btnAddAdmin).setOnClickListener {
            showAddAdminDialog()
        }

        refreshUI()
    }

    private fun refreshUI() {
        tvCollegesCount.text = DataManager.colleges.size.toString()
        tvAdminsCount.text = DataManager.admins.size.toString()
        tvStudentsCount.text = DataManager.colleges.map { it.studentCount }.sum().toString()

        refreshCollegesTable()
        refreshAdminsTable()
    }

    private fun refreshCollegesTable() {
        if (tlColleges.childCount > 2) {
            tlColleges.removeViews(2, tlColleges.childCount - 2)
        }

        for (college in DataManager.colleges) {
            val row = TableRow(this)
            row.setPadding(0, dpToPx(12), 0, dpToPx(12))

            row.addView(createTableTextView(college.name))
            row.addView(createTableTextView(college.adminCount.toString()))
            row.addView(createTableTextView(college.studentCount.toString()))
            row.addView(createStatusTextView(college.status))
            row.addView(createDeleteButton {
                showDeleteCollegeConfirmation(college)
            })

            tlColleges.addView(row)
        }
    }

    private fun refreshAdminsTable() {
        if (tlAdmins.childCount > 2) {
            tlAdmins.removeViews(2, tlAdmins.childCount - 2)
        }

        for (admin in DataManager.admins) {
            val row = TableRow(this)
            row.setPadding(0, dpToPx(12), 0, dpToPx(12))

            row.addView(createTableTextView(admin.name))
            row.addView(createTableTextView(admin.email))
            row.addView(createTableTextView(admin.college))
            row.addView(createDeleteButton {
                showDeleteAdminConfirmation(admin)
            })

            tlAdmins.addView(row)
        }
    }

    private fun createTableTextView(text: String): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.setTextColor(ContextCompat.getColor(this, R.color.black))
        tv.textSize = 11f
        tv.setPadding(0, 0, dpToPx(16), 0)
        return tv
    }

    private fun createStatusTextView(status: String): TextView {
        val tv = TextView(this)
        tv.text = status
        tv.textSize = 10f
        tv.setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))
        tv.background = ContextCompat.getDrawable(this, R.drawable.bg_new_badge)
        if (status == "active") {
            tv.backgroundTintList = ContextCompat.getColorStateList(this, R.color.success_green)
            tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        tv.gravity = Gravity.CENTER
        return tv
    }

    private fun createDeleteButton(onClick: () -> Unit): ImageView {
        val iv = ImageView(this)
        iv.setImageResource(R.drawable.ic_delete)
        val size = dpToPx(20)
        iv.layoutParams = TableRow.LayoutParams(size, size)
        iv.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
        iv.setColorFilter(ContextCompat.getColor(this, R.color.error_red))
        iv.setOnClickListener { onClick() }
        return iv
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun showAddCollegeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_college, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCollegeName)
        val etAdmins = dialogView.findViewById<EditText>(R.id.etAdminCount)
        val etStudents = dialogView.findViewById<EditText>(R.id.etStudentCount)

        AlertDialog.Builder(this)
            .setTitle("Add College")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val admins = etAdmins.text.toString().toIntOrNull() ?: 0
                val students = etStudents.text.toString().toIntOrNull() ?: 0
                if (name.isNotEmpty()) {
                    DataManager.colleges.add(College(name, admins, students))
                    refreshUI()
                    Toast.makeText(this, "$name added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddAdminDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin, null)
        val etName = dialogView.findViewById<EditText>(R.id.etAdminName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etAdminEmail)
        val spinnerCollege = dialogView.findViewById<Spinner>(R.id.spinnerCollege)

        val collegeNames = DataManager.colleges.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, collegeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCollege.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Add Administrator")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val college = spinnerCollege.selectedItem?.toString() ?: ""
                if (name.isNotEmpty() && email.isNotEmpty() && college.isNotEmpty()) {
                    DataManager.admins.add(Administrator(name, email, college))
                    refreshUI()
                    Toast.makeText(this, "Admin $name added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCollegeConfirmation(college: College) {
        AlertDialog.Builder(this)
            .setTitle("Delete College")
            .setMessage("Are you sure you want to delete ${college.name}?")
            .setPositiveButton("Delete") { _, _ ->
                DataManager.colleges.remove(college)
                refreshUI()
                Toast.makeText(this, "${college.name} deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAdminConfirmation(admin: Administrator) {
        AlertDialog.Builder(this)
            .setTitle("Delete Administrator")
            .setMessage("Are you sure you want to delete ${admin.name}?")
            .setPositiveButton("Delete") { _, _ ->
                DataManager.admins.remove(admin)
                refreshUI()
                Toast.makeText(this, "Admin ${admin.name} deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
