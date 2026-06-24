package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArchivedIssuesActivity : AppCompatActivity() {

    private lateinit var rvArchivedIssues: RecyclerView
    private lateinit var tvArchiveCount: TextView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var adapter: AllIssuesAdapter
    
    private var archivedList = mutableListOf<FacultyIssue>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_archived_issues)

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupRecyclerView()
        loadArchivedIssues()
        setupListeners()
    }

    private fun initializeViews() {
        rvArchivedIssues = findViewById(R.id.rvArchivedIssues)
        tvArchiveCount = findViewById(R.id.tvArchiveCount)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupRecyclerView() {
        // Pass a callback to refresh the list when an action (Unarchive/Delete) is performed
        adapter = AllIssuesAdapter(archivedList) {
            loadArchivedIssues()
        }
        rvArchivedIssues.layoutManager = LinearLayoutManager(this)
        rvArchivedIssues.adapter = adapter
    }

    private fun loadArchivedIssues() {
        archivedList.clear()
        archivedList.addAll(DataManager.facultyIssues.filter { it.isArchived })
        
        tvArchiveCount.text = "${archivedList.size} archived records"
        
        if (archivedList.isEmpty()) {
            llEmptyState.visibility = View.VISIBLE
            rvArchivedIssues.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvArchivedIssues.visibility = View.VISIBLE
        }
        
        adapter.updateData(archivedList)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadArchivedIssues()
    }
}