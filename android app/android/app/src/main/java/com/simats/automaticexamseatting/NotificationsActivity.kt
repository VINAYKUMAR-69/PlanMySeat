package com.simats.automaticexamseatting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.automaticexamseatting.network.MessageResponse
import com.simats.automaticexamseatting.network.NotificationResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var rvNotifications: RecyclerView
    private var notificationList = mutableListOf<Notification>()
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupStaticViews()
        fetchNotifications()
    }

    private fun setupRecyclerView() {
        rvNotifications = findViewById(R.id.rvNotifications)
        adapter = NotificationAdapter(notificationList) { notification ->
            showDeleteConfirmation(notification)
        }
        rvNotifications.adapter = adapter
    }

    private fun showDeleteConfirmation(notification: Notification) {
        AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNotificationFromServer(notification.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupStaticViews() {
        findViewById<ImageView>(R.id.ivBack)?.setOnClickListener { finish() }
        
        findViewById<ImageView>(R.id.ivMenu)?.setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<ImageView>(R.id.ivSettings)?.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }
    }

    private fun fetchNotifications() {
        if (userEmail.isEmpty()) return

        RetrofitClient.instance.getNotifications(userEmail).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val remoteNotifs = response.body()!!
                    notificationList.clear()
                    remoteNotifs.forEach { res ->
                        notificationList.add(Notification(
                            res.id, 
                            res.title ?: "No Title", 
                            res.message ?: "No Message", 
                            "${res.date ?: ""} ${res.time ?: ""}".trim(), 
                            false
                        ))
                    }
                    refreshNotificationsUI()
                }
            }
            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                Log.e("Notifications", "Error: ${t.message}")
                Toast.makeText(this@NotificationsActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteNotificationFromServer(id: Int) {
        RetrofitClient.instance.deleteNotification(id).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    notificationList.removeAll { it.id == id }
                    refreshNotificationsUI()
                    Toast.makeText(this@NotificationsActivity, "Notification deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@NotificationsActivity, "Failed to delete notification", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Log.e("Notifications", "Delete error: ${t.message}")
                Toast.makeText(this@NotificationsActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshNotificationsUI() {
        adapter.updateData(notificationList)
        updateCountUI(notificationList.size)
    }

    private fun updateCountUI(count: Int) {
        val unreadCount = notificationList.count { !it.isRead }
        val statusText = if (count > 0) "$count notifications" else "No notifications"
        findViewById<TextView>(R.id.tvNotificationStatus)?.text = statusText
        
        val headerCount = findViewById<TextView>(R.id.tvHeaderNotificationCount)
        if (headerCount != null) {
            if (unreadCount > 0) {
                headerCount.text = unreadCount.toString()
                headerCount.visibility = View.VISIBLE
            } else {
                headerCount.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchNotifications()
    }
}
