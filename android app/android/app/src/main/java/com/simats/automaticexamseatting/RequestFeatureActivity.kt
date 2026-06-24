package com.simats.automaticexamseatting

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestFeatureActivity : AppCompatActivity() {

    private lateinit var etFeatureTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerPriority: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etUseCase: EditText
    private lateinit var etBenefit: EditText
    private lateinit var btnSubmitRequest: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_feature)

        val headerCard = findViewById<android.view.View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        initializeViews()
        setupSpinners()
        setupListeners()
    }

    private fun initializeViews() {
        etFeatureTitle = findViewById(R.id.etFeatureTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        etDescription = findViewById(R.id.etDescription)
        etUseCase = findViewById(R.id.etUseCase)
        etBenefit = findViewById(R.id.etBenefit)
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSpinners() {
        // Categories
        val categories = arrayOf("Select Category", "User Interface", "Performance", "New Functionality", "Security", "Other")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Priorities
        val priorities = arrayOf("Low", "Medium", "High", "Critical")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
        spinnerPriority.setSelection(1) // Default to Medium
    }

    private fun setupListeners() {
        btnSubmitRequest.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val title = etFeatureTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val priority = spinnerPriority.selectedItem.toString()
        val description = etDescription.text.toString().trim()
        val useCase = etUseCase.text.toString().trim()
        val benefit = etBenefit.text.toString().trim()

        if (title.isEmpty()) {
            etFeatureTitle.error = "Title is required"
            return
        }

        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Description is required"
            return
        }

        val request = FeatureRequestCreate(
            featureTitle = title,
            category = category,
            priority = priority,
            description = description,
            useCase = useCase,
            expectedBenefit = benefit
        )

        btnSubmitRequest.isEnabled = false
        btnSubmitRequest.text = "Submitting..."

        RetrofitClient.instance.submitFeatureRequest(request).enqueue(object : Callback<SubmitFeatureRequestResponse> {
            override fun onResponse(call: Call<SubmitFeatureRequestResponse>, response: Response<SubmitFeatureRequestResponse>) {
                btnSubmitRequest.isEnabled = true
                btnSubmitRequest.text = "Submit Request"
                
                if (response.isSuccessful) {
                    Toast.makeText(this@RequestFeatureActivity, "Feature request submitted successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@RequestFeatureActivity, "Failed to submit request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubmitFeatureRequestResponse>, t: Throwable) {
                btnSubmitRequest.isEnabled = true
                btnSubmitRequest.text = "Submit Request"
                Toast.makeText(this@RequestFeatureActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
