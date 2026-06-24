package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BulkOperationsActivity : AppCompatActivity() {

    private lateinit var llBulkStudentList: LinearLayout
    private lateinit var tvSelectionCount: TextView
    private lateinit var cbSelectAll: CheckBox
    private lateinit var spinnerBranch: Spinner
    private lateinit var spinnerYear: Spinner
    
    private val selectedStudents = mutableSetOf<String>() // Set of RegNos
    private var filteredStudents = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bulk_operations)
        
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupFilters()
        setupListeners()
        loadStudents()
    }

    private fun initializeViews() {
        llBulkStudentList = findViewById(R.id.llBulkStudentList)
        tvSelectionCount = findViewById(R.id.tvSelectionCount)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        spinnerBranch = findViewById(R.id.spinnerBranch)
        spinnerYear = findViewById(R.id.spinnerYear)
        
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }
    }

    private fun setupFilters() {
        val branches = mutableListOf("All Branches")
        branches.addAll(DataManager.students.map { it.branch }.distinct())
        val branchAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBranch.adapter = branchAdapter

        val years = mutableListOf("All Years")
        years.addAll(DataManager.students.map { it.year.toString() }.distinct().sorted())
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadStudents()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerBranch.onItemSelectedListener = filterListener
        spinnerYear.onItemSelectedListener = filterListener
    }

    private fun setupListeners() {
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filteredStudents.forEach { selectedStudents.add(it.regNo) }
            } else {
                selectedStudents.clear()
            }
            updateListCheckboxes()
            updateSelectionUI()
        }

        findViewById<View>(R.id.btnDelete).setOnClickListener {
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "No students selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDeleteConfirmDialog()
        }

        findViewById<View>(R.id.btnExport).setOnClickListener {
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "No students selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Exporting ${selectedStudents.size} students to CSV...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStudents() {
        llBulkStudentList.removeAllViews()
        val branchFilter = spinnerBranch.selectedItem?.toString() ?: "All Branches"
        val yearFilter = spinnerYear.selectedItem?.toString() ?: "All Years"

        filteredStudents = DataManager.students.filter { student ->
            (branchFilter == "All Branches" || student.branch == branchFilter) &&
            (yearFilter == "All Years" || student.year.toString() == yearFilter)
        }.toMutableList()

        filteredStudents.forEach { student ->
            val itemView = layoutInflater.inflate(R.layout.item_student_bulk, llBulkStudentList, false)
            
            val cb = itemView.findViewById<CheckBox>(R.id.cbStudent)
            cb.isChecked = selectedStudents.contains(student.regNo)
            cb.setOnClickListener {
                if (cb.isChecked) {
                    selectedStudents.add(student.regNo)
                } else {
                    selectedStudents.remove(student.regNo)
                    cbSelectAll.isChecked = false
                }
                updateSelectionUI()
            }

            itemView.findViewById<TextView>(R.id.tvStudentName).text = student.name
            itemView.findViewById<TextView>(R.id.tvRegNo).text = student.regNo
            itemView.findViewById<TextView>(R.id.tvBranch).text = student.branch

            llBulkStudentList.addView(itemView)
        }
        updateSelectionUI()
    }

    private fun updateListCheckboxes() {
        for (i in 0 until llBulkStudentList.childCount) {
            val view = llBulkStudentList.getChildAt(i)
            val cb = view.findViewById<CheckBox>(R.id.cbStudent)
            val regNo = view.findViewById<TextView>(R.id.tvRegNo).text.toString()
            cb.isChecked = selectedStudents.contains(regNo)
        }
    }

    private fun updateSelectionUI() {
        tvSelectionCount.text = "${selectedStudents.size} selected"
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete ${selectedStudents.size} students from the server?")
            .setPositiveButton("Delete") { _, _ ->
                val regNosToDelete = selectedStudents.toList()
                RetrofitClient.instance.deleteBulkStudents(regNosToDelete).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        if (response.isSuccessful) {
                            DataManager.students.removeAll { selectedStudents.contains(it.regNo) }
                            selectedStudents.clear()
                            cbSelectAll.isChecked = false
                            loadStudents()
                            Toast.makeText(this@BulkOperationsActivity, "Students deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@BulkOperationsActivity, "Failed to delete: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        Toast.makeText(this@BulkOperationsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
