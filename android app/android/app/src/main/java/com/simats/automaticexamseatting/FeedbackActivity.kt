package com.simats.automaticexamseatting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var rgFeedbackType: RadioGroup
    private lateinit var ratingBar: RatingBar
    private lateinit var etMessage: EditText
    private lateinit var btnSubmit: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerCard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        rgFeedbackType = findViewById(R.id.rgFeedbackType)
        ratingBar = findViewById(R.id.ratingBar)
        etMessage = findViewById(R.id.etMessage)
        btnSubmit = findViewById(R.id.btnSubmitFeedback)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
        
        // Pre-fill user data if available
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        etName.setText(sharedPref.getString("user_name", ""))
        etEmail.setText(sharedPref.getString("user_email", ""))
    }

    private fun setupListeners() {
        btnSubmit.setOnClickListener {
            submitFeedback()
        }

        findViewById<View>(R.id.tvSupportLink).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@examseating.com")
            }
            startActivity(Intent.createChooser(intent, "Contact Support"))
        }
    }

    private fun submitFeedback() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val message = etMessage.text.toString().trim()
        val rating = ratingBar.rating.toInt()

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedTypeId = rgFeedbackType.checkedRadioButtonId
        val feedbackType = if (selectedTypeId != -1) {
            findViewById<RadioButton>(selectedTypeId).text.toString()
        } else {
            "General"
        }

        val request = FeedbackRequest(
            name = name,
            email = email,
            feedbackType = feedbackType,
            rating = rating,
            message = message
        )

        btnSubmit.isEnabled = false
        Toast.makeText(this, "Submitting feedback...", Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.submitFeedback(request).enqueue(object : Callback<SubmitFeedbackResponse> {
            override fun onResponse(call: Call<SubmitFeedbackResponse>, response: Response<SubmitFeedbackResponse>) {
                btnSubmit.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@FeedbackActivity, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Log.e("Feedback", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@FeedbackActivity, "Failed to submit feedback. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubmitFeedbackResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                Log.e("Feedback", "Failure: ${t.message}")
                Toast.makeText(this@FeedbackActivity, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
