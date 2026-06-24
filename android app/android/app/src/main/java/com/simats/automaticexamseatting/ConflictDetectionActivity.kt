package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConflictDetectionActivity : AppCompatActivity() {

    private lateinit var tvTotalConflicts: TextView
    private lateinit var tvRoomsAffected: TextView
    private lateinit var tvConflictRate: TextView
    private lateinit var cvStatusBanner: CardView
    private lateinit var llBannerBackground: LinearLayout
    private lateinit var ivStatusIcon: ImageView
    private lateinit var tvStatusTitle: TextView
    private lateinit var tvStatusDesc: TextView
    private lateinit var llBranchAnalysisContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conflict_detection)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
        performConflictAnalysis()
    }

    private fun initializeViews() {
        tvTotalConflicts = findViewById(R.id.tvTotalConflicts)
        tvRoomsAffected = findViewById(R.id.tvRoomsAffected)
        tvConflictRate = findViewById(R.id.tvConflictRate)
        cvStatusBanner = findViewById(R.id.cvStatusBanner)
        llBannerBackground = findViewById(R.id.llBannerBackground)
        ivStatusIcon = findViewById(R.id.ivStatusIcon)
        tvStatusTitle = findViewById(R.id.tvStatusTitle)
        tvStatusDesc = findViewById(R.id.tvStatusDesc)
        llBranchAnalysisContainer = findViewById(R.id.llBranchAnalysisContainer)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }
        
        findViewById<CardView>(R.id.cvProfile).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    private fun performConflictAnalysis() {
        val report = DataManager.currentExam
        if (report == null || report.allocations.isEmpty()) {
            updateUIForNoData()
            return
        }

        val allocations = report.allocations
        val allocationsByRoom = allocations.groupBy { it.roomNumber }
        
        var totalConflicts = 0
        val affectedRooms = mutableSetOf<String>()
        val conflictsByBranch = mutableMapOf<String, Int>()

        allocationsByRoom.forEach { (roomNumber, roomAllocations) ->
            for (i in 0 until roomAllocations.size - 1) {
                val current = roomAllocations[i]
                val next = roomAllocations[i + 1]
                
                if (current.branch == next.branch) {
                    totalConflicts++
                    affectedRooms.add(roomNumber)
                    conflictsByBranch[current.branch] = (conflictsByBranch[current.branch] ?: 0) + 1
                }
            }
        }

        val conflictRate = if (allocations.size > 1) {
            (totalConflicts.toDouble() / (allocations.size - 1) * 100).toInt()
        } else 0

        updateUI(totalConflicts, affectedRooms.size, conflictRate, conflictsByBranch)
    }

    private fun updateUIForNoData() {
        tvTotalConflicts.text = "0"
        tvRoomsAffected.text = "0"
        tvConflictRate.text = "0%"
        tvStatusTitle.text = "No Data Available"
        tvStatusDesc.text = "Generate a seating plan first to detect conflicts."
    }

    private fun updateUI(total: Int, rooms: Int, rate: Int, branchConflicts: Map<String, Int>) {
        tvTotalConflicts.text = total.toString()
        tvRoomsAffected.text = rooms.toString()
        tvConflictRate.text = "$rate%"

        if (total == 0) {
            tvTotalConflicts.setTextColor(Color.parseColor("#10B981"))
            llBannerBackground.setBackgroundColor(Color.parseColor("#F0FDF4"))
            ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
            ivStatusIcon.setColorFilter(Color.parseColor("#10B981"))
            tvStatusTitle.text = "No Conflicts Found"
            tvStatusTitle.setTextColor(Color.parseColor("#166534"))
            tvStatusDesc.text = "All students are properly separated by branch."
            tvStatusDesc.setTextColor(Color.parseColor("#166534"))
        } else {
            tvTotalConflicts.setTextColor(Color.parseColor("#EF4444"))
            llBannerBackground.setBackgroundColor(Color.parseColor("#FEF2F2"))
            ivStatusIcon.setImageResource(R.drawable.ic_error)
            ivStatusIcon.setColorFilter(Color.parseColor("#EF4444"))
            tvStatusTitle.text = "$total Conflicts Detected"
            tvStatusTitle.setTextColor(Color.parseColor("#991B1B"))
            tvStatusDesc.text = "Students of the same branch are sitting together in $rooms rooms."
            tvStatusDesc.setTextColor(Color.parseColor("#991B1B"))
            
            llBranchAnalysisContainer.removeAllViews()
            branchConflicts.forEach { (branch, count) ->
                val branchView = layoutInflater.inflate(android.R.layout.simple_list_item_2, llBranchAnalysisContainer, false)
                branchView.findViewById<TextView>(android.R.id.text1).text = branch
                branchView.findViewById<TextView>(android.R.id.text2).text = "$count conflicts"
                llBranchAnalysisContainer.addView(branchView)
            }
        }
    }
}