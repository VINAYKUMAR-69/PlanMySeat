package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.RegisterRequest
import com.simats.automaticexamseatting.network.RegisterResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etCollege: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var tvLogin: TextView
    private lateinit var ivShowPassword: ImageView
    private lateinit var ivShowConfirmPassword: ImageView
    
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etCollege = findViewById(R.id.etCollege)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvLogin = findViewById(R.id.tvLogin)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        ivShowConfirmPassword = findViewById(R.id.ivShowConfirmPassword)

        ivShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        ivShowConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        btnCreateAccount.setOnClickListener {
            performSignUp()
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            ivShowPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            ivShowPassword.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility)
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        etConfirmPassword.setSelection(etConfirmPassword.text.length)
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Registration Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun performSignUp() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim().lowercase()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val college = etCollege.text.toString().trim()

        var hasError = false
        val errorMessage = StringBuilder()

        if (fullName.isEmpty()) {
            etFullName.error = "Full Name is required"
            errorMessage.append("- Please enter your full name\n")
            hasError = true
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            errorMessage.append("- Please enter your email address\n")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            errorMessage.append("- Please enter a valid email address\n")
            hasError = true
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            errorMessage.append("- Please enter a password\n")
            hasError = true
        } else if (!password.contains(Regex("[A-Z]"))) {
            etPassword.error = "Must contain at least one capital letter"
            errorMessage.append("- Password must have a capital letter\n")
            hasError = true
        } else if (!password.contains(Regex("[0-9]"))) {
            etPassword.error = "Must contain at least one number"
            errorMessage.append("- Password must have a number\n")
            hasError = true
        } else if (!password.contains(Regex("[@#\$%^&+=!]"))) {
            etPassword.error = "Must contain at least one special character (@#\$%^&+=!)"
            errorMessage.append("- Password must have a special character\n")
            hasError = true
        } else if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            errorMessage.append("- Password too short\n")
            hasError = true
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            errorMessage.append("- Passwords do not match\n")
            hasError = true
        }

        if (college.isEmpty()) {
            etCollege.error = "College/Organization is required"
            errorMessage.append("- Please enter your college or organization\n")
            hasError = true
        }

        if (hasError) {
            showErrorDialog(errorMessage.toString().trim())
            return
        }

        val role = "Student"
        val request = RegisterRequest(fullName, email, password, role, college)

        RetrofitClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "Registration Successful for $email", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    showErrorDialog("Server Error: $errorMsg")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showErrorDialog("Network Error: ${t.message}")
            }
        })
    }
}
