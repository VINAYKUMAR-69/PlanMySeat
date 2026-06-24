package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class TermsPrivacyActivity : AppCompatActivity() {

    private lateinit var tvTabTerms: TextView
    private lateinit var tvTabPrivacy: TextView
    private lateinit var tvContentTitle: TextView
    private lateinit var tvLegalText: TextView
    private lateinit var ivContentIcon: ImageView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms_privacy)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerCard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
        showTerms() // Default tab
    }

    private fun initializeViews() {
        tvTabTerms = findViewById(R.id.tvTabTerms)
        tvTabPrivacy = findViewById(R.id.tvTabPrivacy)
        tvContentTitle = findViewById(R.id.tvContentTitle)
        tvLegalText = findViewById(R.id.tvLegalText)
        ivContentIcon = findViewById(R.id.ivContentIcon)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        tvTabTerms.setOnClickListener { showTerms() }
        tvTabPrivacy.setOnClickListener { showPrivacy() }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); finish(); true }
                R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); finish(); true }
                R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); finish(); true }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfileActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }

    private fun showTerms() {
        // Update UI
        tvTabTerms.setBackgroundResource(R.drawable.bg_button)
        tvTabTerms.setTextColor(Color.WHITE)
        tvTabPrivacy.background = null
        tvTabPrivacy.setTextColor(Color.parseColor("#64748B"))

        tvContentTitle.text = "Terms of Service"
        ivContentIcon.setImageResource(R.drawable.ic_reports)

        val terms = """
            <b>1. Acceptance of Terms</b><br/>
            By accessing and using the Exam Seating System, you accept and agree to be bound by the terms and provisions of this agreement.<br/><br/>
            
            <b>2. Use License</b><br/>
            Permission is granted to temporarily use this system for educational institution purposes only. This is the grant of a license, not a transfer of title.<br/><br/>
            
            <b>3. User Responsibilities</b><br/>
            Users are responsible for maintaining the confidentiality of their account credentials and for all activities that occur under their account.<br/><br/>
            
            <b>4. Data Accuracy</b><br/>
            While we strive to ensure the seating allocation algorithm works correctly, users should always verify results before finalizing exam plans.<br/><br/>
            
            <b>5. Limitation of Liability</b><br/>
            The system is provided "as is" without any warranties. We shall not be liable for any damages arising from the use or inability to use this system.
        """.trimIndent()
        
        tvLegalText.text = Html.fromHtml(terms, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun showPrivacy() {
        // Update UI
        tvTabPrivacy.setBackgroundResource(R.drawable.bg_button)
        tvTabPrivacy.setTextColor(Color.WHITE)
        tvTabTerms.background = null
        tvTabTerms.setTextColor(Color.parseColor("#64748B"))

        tvContentTitle.text = "Privacy Policy"
        ivContentIcon.setImageResource(R.drawable.ic_lock)

        val privacy = """
            <b>1. Information Collection</b><br/>
            We collect student and room data solely for the purpose of generating seating arrangements as requested by the institution.<br/><br/>
            
            <b>2. Data Usage</b><br/>
            The data entered into the system is used only to perform automated allocation and generate administrative reports.<br/><br/>
            
            <b>3. Data Security</b><br/>
            We implement industry-standard security measures to protect institution data from unauthorized access or disclosure.<br/><br/>
            
            <b>4. Data Sharing</b><br/>
            We do not sell, trade, or otherwise transfer your data to outside parties without your explicit consent.<br/><br/>
            
            <b>5. Policy Updates</b><br/>
            We may update this privacy policy from time to time. Users will be notified of any significant changes via the system.
        """.trimIndent()
        
        tvLegalText.text = Html.fromHtml(privacy, Html.FROM_HTML_MODE_COMPACT)
    }
}
