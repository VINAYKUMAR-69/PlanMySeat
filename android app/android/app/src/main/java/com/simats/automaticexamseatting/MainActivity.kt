package com.simats.automaticexamseatting

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.LoginRequest
import com.simats.automaticexamseatting.network.LoginResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var ivShowPassword: ImageView
    private var isPasswordVisible = false

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
        setContentView(R.layout.activity_main)

        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp = findViewById(R.id.tvSignUp)
        ivShowPassword = findViewById(R.id.ivShowPassword)

        ivShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnLogin.setOnClickListener {
            performLogin()
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
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

    private fun performLogin() {
        val email = etEmail.text.toString().trim().lowercase()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        // SAVE EMAIL TO PREFERENCES (Consistently lowercase)
                        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit {
                            putString("user_email", email)
                            putString("user_name", user.full_name)
                            putString("user_role", user.role)
                            apply()
                        }

                        Toast.makeText(
                            this@MainActivity,
                            user.message ?: "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        intent.putExtra("role", user.role)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Invalid Credentials",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Connection Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
