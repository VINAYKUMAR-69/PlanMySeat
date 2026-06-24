package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class AllIssuesActivity : AppCompatActivity() {

    private lateinit var rvAllIssues: RecyclerView
    private lateinit var adapter: AllIssuesAdapter
    private lateinit var etSearchIssues: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerStatus: Spinner
    
    private var fullList = mutableListOf<FacultyIssue>()
    private var filteredList = mutableListOf<FacultyIssue>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_issues)

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupFilters()
        setupListeners()
    }

    private fun initializeViews() {
        rvAllIssues = findViewById(R.id.rvAllIssues)
        etSearchIssues = findViewById(R.id.etSearchIssues)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerStatus = findViewById(R.id.spinnerStatus)
    }

    private fun setupRecyclerView() {
        adapter = AllIssuesAdapter(filteredList) {
            refreshData()
        }
        rvAllIssues.layoutManager = LinearLayoutManager(this)
        rvAllIssues.adapter = adapter
    }

    private fun refreshData() {
        fullList.clear()
        // Only show non-archived issues in the main list
        fullList.addAll(DataManager.facultyIssues.filter { !it.isArchived })
        applyFilters()
    }

    private fun setupFilters() {
        val categories = listOf("All Categories", "Academic", "Attendance", "Behavioral", "Health", "Financial")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        val statuses = listOf("All Statuses", "Open", "In Progress", "Resolved")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCategory.onItemSelectedListener = filterListener
        spinnerStatus.onItemSelectedListener = filterListener
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnReportIssueTop).setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        etSearchIssues.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val query = etSearchIssues.text.toString().lowercase()
        val categoryFilter = spinnerCategory.selectedItem?.toString() ?: "All Categories"
        val statusFilter = spinnerStatus.selectedItem?.toString() ?: "All Statuses"

        val results = fullList.filter { issue ->
            val matchesQuery = issue.title.lowercase().contains(query) || 
                             issue.studentName.lowercase().contains(query) ||
                             issue.description.lowercase().contains(query)
            
            val matchesCategory = categoryFilter == "All Categories" || issue.category == categoryFilter
            val matchesStatus = statusFilter == "All Statuses" || issue.status == statusFilter

            matchesQuery && matchesCategory && matchesStatus
        }

        filteredList.clear()
        filteredList.addAll(results)
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }
}