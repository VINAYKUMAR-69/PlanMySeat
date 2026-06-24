package com.simats.automaticexamseatting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-login check: If user details exist in SharedPreferences, go straight to Dashboard
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("user_email", null)
        val role = sharedPref.getString("user_role", null)
        if (!email.isNullOrEmpty() && !role.isNullOrEmpty()) {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("role", role)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // Adjust for system bars
        val mainLayout = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Rotate sparkle icons
        val iconLeft = findViewById<TextView>(R.id.iconLeft)
        val iconRight = findViewById<TextView>(R.id.iconRight)

        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 3000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }

        iconLeft?.startAnimation(rotateAnimation)
        iconRight?.startAnimation(rotateAnimation)

        // Main Action Buttons
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        btnGetStarted.setOnClickListener {
            // Navigate to Onboarding flow
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }

        tvSignIn.setOnClickListener {
            // Navigate to Login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Interactive Feature Tags (Chips)
        findViewById<TextView>(R.id.tagAllocation).setOnClickListener {
            showFeatureInfo("Automated Allocation: Smart AI seating logic for conflict-free exams.")
        }

        findViewById<TextView>(R.id.tagAnalytics).setOnClickListener {
            showFeatureInfo("Real-time Analytics: Live dashboard monitoring exam progress.")
        }

        findViewById<TextView>(R.id.tagFaculty).setOnClickListener {
            showFeatureInfo("Faculty Management: Streamlined invigilation assignments.")
        }

        findViewById<TextView>(R.id.tagMobile).setOnClickListener {
            showFeatureInfo("Mobile Friendly: Accessible across all modern devices.")
        }
    }

    private fun showFeatureInfo(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
