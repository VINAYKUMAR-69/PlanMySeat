package com.simats.automaticexamseatting

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ReportBugActivity : AppCompatActivity() {

    private lateinit var etBugTitle: EditText
    private lateinit var spinnerSeverity: Spinner
    private lateinit var spinnerFrequency: Spinner
    private lateinit var etBugDescription: EditText
    private lateinit var etSteps: EditText
    private lateinit var etExpected: EditText
    private lateinit var etActual: EditText
    private lateinit var btnSubmitBug: MaterialButton
    
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
            // In a real app, you might show a preview or the file name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_bug)

        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupSpinners()
        setupListeners()
    }

    private fun initializeViews() {
        etBugTitle = findViewById(R.id.etBugTitle)
        spinnerSeverity = findViewById(R.id.spinnerSeverity)
        spinnerFrequency = findViewById(R.id.spinnerFrequency)
        etBugDescription = findViewById(R.id.etBugDescription)
        etSteps = findViewById(R.id.etSteps)
        etExpected = findViewById(R.id.etExpected)
        etActual = findViewById(R.id.etActual)
        btnSubmitBug = findViewById(R.id.btnSubmitBug)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSpinners() {
        // Severities
        val severities = arrayOf("Low", "Medium", "High", "Critical")
        val severityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, severities)
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = severityAdapter
        spinnerSeverity.setSelection(1) // Default to Medium

        // Frequencies
        val frequencies = arrayOf("Every time", "Sometimes", "Rarely", "Once")
        val frequencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = frequencyAdapter
        spinnerFrequency.setSelection(1) // Default to Sometimes
    }

    private fun setupListeners() {
        btnSubmitBug.setOnClickListener {
            validateAndSubmit()
        }
        
        findViewById<android.view.View>(R.id.btnUpload).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun validateAndSubmit() {
        val title = etBugTitle.text.toString().trim()
        val description = etBugDescription.text.toString().trim()
        val steps = etSteps.text.toString().trim()

        if (title.isEmpty()) {
            etBugTitle.error = "Bug title is required"
            return
        }

        if (description.isEmpty()) {
            etBugDescription.error = "Description is required"
            return
        }

        if (steps.isEmpty()) {
            etSteps.error = "Steps to reproduce are required"
            return
        }

        submitBugToApi()
    }

    private fun submitBugToApi() {
        val title = etBugTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val severity = spinnerSeverity.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val frequency = spinnerFrequency.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val description = etBugDescription.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val steps = etSteps.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val expected = etExpected.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val actual = etActual.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        selectedImageUri?.let { uri ->
            try {
                val file = File(cacheDir, "bug_screenshot.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("screenshot", file.name, requestFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnSubmitBug.isEnabled = false
        btnSubmitBug.text = "Submitting..."

        RetrofitClient.instance.submitBugReport(
            title, severity, frequency, description, steps, expected, actual, imagePart
        ).enqueue(object : Callback<BugReportSubmitResponse> {
            override fun onResponse(call: Call<BugReportSubmitResponse>, response: Response<BugReportSubmitResponse>) {
                btnSubmitBug.isEnabled = true
                btnSubmitBug.text = "Submit Bug Report"
                
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportBugActivity, "Bug report submitted successfully! Thank you for your feedback.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@ReportBugActivity, "Failed to submit bug: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BugReportSubmitResponse>, t: Throwable) {
                btnSubmitBug.isEnabled = true
                btnSubmitBug.text = "Submit Bug Report"
                Toast.makeText(this@ReportBugActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
