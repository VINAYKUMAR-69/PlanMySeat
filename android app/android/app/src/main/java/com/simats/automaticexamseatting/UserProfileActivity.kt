package com.simats.automaticexamseatting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class UserProfileActivity : AppCompatActivity() {
    
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCollege: EditText
    private lateinit var tvExamsCreatedCount: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvLastLogin: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var cvAvatar: CardView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var ivProfilePhoto: ImageView

    private var currentUserEmail: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null && currentUserEmail != null) {
                uploadPhoto(currentUserEmail!!, imageUri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null && currentUserEmail != null) {
                uploadPhoto(currentUserEmail!!, imageBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Get saved email from login
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("user_email", null)?.lowercase()?.trim()

        initializeViews()
        setupListeners()
        
        if (!currentUserEmail.isNullOrEmpty()) {
            fetchProfile(currentUserEmail!!)
        } else {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            performLogout()
        }
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etCollege = findViewById(R.id.etCollege)
        
        tvExamsCreatedCount = findViewById(R.id.tvExamsCreatedCount)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvLastLogin = findViewById(R.id.tvLastLogin)
        
        cvAvatar = findViewById(R.id.cvAvatar)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)

        try {
            bottomNavigation = findViewById(R.id.bottomNavigation)
            bottomNavigation.selectedItemId = R.id.nav_profile
        } catch (e: Exception) {}
    }

    private fun fetchProfile(email: String) {
        RetrofitClient.instance.getProfile(email).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    profile?.let {
                        etFullName.setText(it.fullName)
                        etEmail.setText(it.email)
                        // Load from collegeOrganization if college is null
                        etCollege.setText(it.collegeOrganization ?: it.college ?: "")
                        
                        tvExamsCreatedCount.text = it.examsCreated.toString()
                        tvMemberSince.text = it.memberSince ?: "N/A"
                        tvLastLogin.text = it.lastLogin ?: "Just now"
                        
                        if (it.fullName.isNotEmpty()) {
                            tvAvatarInitial.text = it.fullName.take(1).uppercase()
                        }

                        if (!it.photo.isNullOrEmpty()) {
                            val fixedPath = it.photo.replace("\\", "/")
                            val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
                            val cleanPath = fixedPath.trimStart('/')
                            val photoUrl = "$baseUrl/$cleanPath"
                            
                            ivProfilePhoto.load(photoUrl) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                                listener(
                                    onSuccess = { _, _ ->
                                        ivProfilePhoto.visibility = View.VISIBLE
                                        tvAvatarInitial.visibility = View.GONE
                                    },
                                    onError = { _, result ->
                                        ivProfilePhoto.visibility = View.GONE
                                        tvAvatarInitial.visibility = View.VISIBLE
                                    }
                                )
                            }
                        } else {
                            ivProfilePhoto.visibility = View.GONE
                            tvAvatarInitial.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.e("Profile", "Error fetching: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile() {
        val email = currentUserEmail ?: return
        val fullName = etFullName.text.toString().trim()
        val college = etCollege.text.toString().trim()

        if (fullName.isEmpty() || college.isEmpty()) {
            Toast.makeText(this, "Name and College cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdate(fullName, college)
        RetrofitClient.instance.updateProfile(email, request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UserProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    fetchProfile(email) 
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update profile"
                    Toast.makeText(this@UserProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Update failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadPhoto(email: String, uri: Uri) {
        val file = File(cacheDir, "temp_profile.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        performPhotoUpload(email, file)
    }

    private fun uploadPhoto(email: String, bitmap: Bitmap) {
        val file = File(cacheDir, "temp_profile.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        performPhotoUpload(email, file)
    }

    private fun performPhotoUpload(email: String, file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.instance.uploadPhoto(email, body).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UserProfileActivity, "Photo uploaded!", Toast.LENGTH_SHORT).show()
                    ivProfilePhoto.postDelayed({ fetchProfile(email) }, 1000)
                } else {
                    Toast.makeText(this@UserProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener { 
            MenuHelper.showCustomMenu(this, it)
        }
        findViewById<MaterialButton>(R.id.btnChangePhoto).setOnClickListener { showImagePickerOptions() }
        findViewById<MaterialButton>(R.id.btnSaveChanges).setOnClickListener { updateProfile() }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener { performLogout() }

        if (::bottomNavigation.isInitialized) {
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this, DashboardActivity::class.java)); true }
                    R.id.nav_students -> { startActivity(Intent(this, StudentsActivity::class.java)); true }
                    R.id.nav_seating -> { startActivity(Intent(this, SeatAllocationSetupActivity::class.java)); true }
                    R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
                    R.id.nav_profile -> true
                    else -> false
                }
            }
        }
    }

    private fun performLogout() {
        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit { clear() }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Profile Photo")
            .setItems(options) { dialog, item ->
                when (options[item]) {
                    "Take Photo" -> takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    "Choose from Gallery" -> pickImageLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                    "Cancel" -> dialog.dismiss()
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavigation.isInitialized) {
            bottomNavigation.selectedItemId = R.id.nav_profile
        }
        // Refresh profile on resume
        currentUserEmail?.let { fetchProfile(it) }
    }
}
