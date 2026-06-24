package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FeedbackHistoryActivity : AppCompatActivity() {

    private lateinit var llFeedbackList: LinearLayout
    private lateinit var etSearch: EditText
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback_history)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initializeViews()
        setupListeners()
        updateStats()
        displayFeedback(DataManager.feedbackHistory)
    }

    private fun initializeViews() {
        llFeedbackList = findViewById(R.id.llFeedbackList)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnAddFeedback).setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }

        findViewById<View>(R.id.btnReportBug).setOnClickListener {
            startActivity(Intent(this, ReportBugActivity::class.java))
        }

        findViewById<View>(R.id.btnRequestFeature).setOnClickListener {
            startActivity(Intent(this, RequestFeatureActivity::class.java))
        }

        findViewById<View>(R.id.btnSendFeedback).setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }

        // Search logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFeedback()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter chips logic
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterBugs = findViewById<TextView>(R.id.filterBugs)
        val filterFeatures = findViewById<TextView>(R.id.filterFeatures)
        val filterIdeas = findViewById<TextView>(R.id.filterIdeas)

        val filters = listOf(filterAll, filterBugs, filterFeatures, filterIdeas)

        filters.forEach { textView ->
            textView.setOnClickListener {
                currentFilter = textView.text.toString()
                
                // Update UI of chips
                filters.forEach { 
                    it.setBackgroundResource(0)
                    it.setTextColor(Color.parseColor("#64748B"))
                }
                textView.setBackgroundResource(R.drawable.bg_new_badge)
                textView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE))
                textView.setTextColor(Color.parseColor("#0F172A"))
                
                filterFeedback()
            }
        }
    }

    private fun updateStats() {
        val total = DataManager.feedbackHistory.size
        val resolved = DataManager.feedbackHistory.count { it.status == "Resolved" }
        val progress = DataManager.feedbackHistory.count { it.status == "In Progress" }
        val review = DataManager.feedbackHistory.count { it.status == "Under Review" }

        findViewById<TextView>(R.id.tvTotalCount).text = total.toString()
        findViewById<TextView>(R.id.tvResolvedCount).text = resolved.toString()
        findViewById<TextView>(R.id.tvProgressCount).text = progress.toString()
        findViewById<TextView>(R.id.tvReviewCount).text = review.toString()
    }

    private fun filterFeedback() {
        val query = etSearch.text.toString().lowercase()
        val filteredList = DataManager.feedbackHistory.filter { 
            val matchesFilter = currentFilter == "All" || 
                               (currentFilter == "Bugs" && it.type == "Bug") ||
                               (currentFilter == "Features" && it.type == "Feature") ||
                               (currentFilter == "Ideas" && it.type == "Idea")
            
            val matchesSearch = it.title.lowercase().contains(query) || 
                                it.description.lowercase().contains(query)
            
            matchesFilter && matchesSearch
        }
        displayFeedback(filteredList)
    }

    private fun displayFeedback(list: List<FeedbackItem>) {
        llFeedbackList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (list.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No feedback found"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 100, 0, 0)
                setTextColor(Color.parseColor("#94A3B8"))
            }
            llFeedbackList.addView(emptyView)
            return
        }

        for (item in list) {
            val itemView = inflater.inflate(R.layout.item_feedback, llFeedbackList, false)
            
            itemView.findViewById<TextView>(R.id.tvFeedbackTitle).text = item.title
            itemView.findViewById<TextView>(R.id.tvFeedbackDesc).text = item.description
            itemView.findViewById<TextView>(R.id.tvStatus).text = item.status.lowercase()
            itemView.findViewById<TextView>(R.id.tvPriority).text = item.priority.lowercase()
            itemView.findViewById<TextView>(R.id.tvDate).text = item.date
            
            val tvResponse = itemView.findViewById<TextView>(R.id.tvResponse)
            val llResponse = itemView.findViewById<LinearLayout>(R.id.llResponse)
            
            if (item.response != null) {
                tvResponse.text = item.response
                llResponse.visibility = View.VISIBLE
            } else {
                llResponse.visibility = View.GONE
            }

            // Type Icon & Color
            val ivType = itemView.findViewById<ImageView>(R.id.ivType)
            val cvTypeIcon = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cvTypeIcon)
            
            when (item.type) {
                "Bug" -> {
                    ivType.setImageResource(R.drawable.ic_error)
                    ivType.setColorFilter(Color.parseColor("#DC2626"))
                    cvTypeIcon.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                "Feature" -> {
                    ivType.setImageResource(R.drawable.ic_ai_magic)
                    ivType.setColorFilter(Color.parseColor("#D97706"))
                    cvTypeIcon.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                }
                "Idea" -> {
                    ivType.setImageResource(R.drawable.ic_email)
                    ivType.setColorFilter(Color.parseColor("#2563EB"))
                    cvTypeIcon.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
                }
            }

            // Status Badge Color
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (item.status) {
                "Resolved" -> {
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                    tvStatus.setTextColor(Color.parseColor("#166534"))
                }
                "In Progress" -> {
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE"))
                    tvStatus.setTextColor(Color.parseColor("#1E40AF"))
                }
                "Under Review" -> {
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                    tvStatus.setTextColor(Color.parseColor("#92400E"))
                }
                "Rejected" -> {
                    tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                    tvStatus.setTextColor(Color.parseColor("#991B1B"))
                }
            }

            llFeedbackList.addView(itemView)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        filterFeedback()
    }
}