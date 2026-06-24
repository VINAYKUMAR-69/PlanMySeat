package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.automaticexamseatting.network.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etResetEmail: EditText
    private lateinit var etOtp: EditText
    private lateinit var btnSendOtp: Button
    private lateinit var btnVerifyOtp: Button
    private lateinit var llOtpSection: LinearLayout
    private lateinit var llBackToLogin: LinearLayout
    
    private var loadingDialog: AlertDialog? = null
    private var tvLoadingMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        val rootLayout = findViewById<View>(R.id.main)
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        etResetEmail = findViewById(R.id.etResetEmail)
        etOtp = findViewById(R.id.etOtp)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        llOtpSection = findViewById(R.id.llOtpSection)
        llBackToLogin = findViewById(R.id.llBackToLogin)

        btnSendOtp.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("ForgotPassword", "Send OTP clicked for: $email")
                sendOtp(email)
            }
        }

        btnVerifyOtp.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            val otp = etOtp.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOtp(email, otp)
            }
        }

        llBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun showLoading(msg: String) {
        if (loadingDialog == null) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
            tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage)
            loadingDialog = AlertDialog.Builder(this).setView(view).setCancelable(false).create()
        }
        tvLoadingMessage?.text = msg
        if (loadingDialog?.isShowing == false) loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun sendOtp(email: String) {
        showLoading("Connecting to server...")
        val request = ForgotPasswordRequest(email)
        
        RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                hideLoading()
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                    llOtpSection.visibility = View.VISIBLE
                    btnSendOtp.text = "Resend OTP"
                } else {
                    val errorMsg = parseErrorMessage(response)
                    Toast.makeText(this@ForgotPasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                hideLoading()
                Log.e("ForgotPassword", "Failed: ${t.message}")
                Toast.makeText(this@ForgotPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun verifyOtp(email: String, otp: String) {
        showLoading("Verifying...")
        val request = VerifyOtpRequest(email, otp)
        RetrofitClient.instance.verifyOtp(request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                hideLoading()
                if (response.isSuccessful) {
                    val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = parseErrorMessage(response)
                    Toast.makeText(this@ForgotPasswordActivity, "Invalid OTP: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                hideLoading()
                Toast.makeText(this@ForgotPasswordActivity, "Verification failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: ""
            val jsonObject = JSONObject(errorBody)
            jsonObject.optString("detail", "Error code: ${response.code()}")
        } catch (e: Exception) {
            "Error: ${response.code()}"
        }
    }
}