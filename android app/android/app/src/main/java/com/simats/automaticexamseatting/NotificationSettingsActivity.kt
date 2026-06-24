package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import android.content.Intent

class NotificationSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
        
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            showMenu(it as ImageView)
        }

        findViewById<MaterialButton>(R.id.btnSaveNotificationSettings).setOnClickListener {
            Toast.makeText(this, "Notification preferences saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showMenu(view: ImageView) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Dashboard")
        popup.menu.add("Students")
        popup.menu.add("Rooms")
        popup.menu.add("Reports")
        popup.menu.add("Bulk Operations")
        popup.menu.add("Manual Adjustment")
        popup.menu.add("Conflict Detection")
        popup.menu.add("Conflict Resolution")
        popup.menu.add("Exam History")
        popup.menu.add("Notifications")
        popup.menu.add("Settings")

        popup.setOnMenuItemClickListener { item: MenuItem ->
            val intent = when (item.title.toString()) {
                "Dashboard" -> Intent(this, DashboardActivity::class.java)
                "Students" -> Intent(this, StudentsActivity::class.java)
                "Rooms" -> Intent(this, RoomsActivity::class.java)
                "Reports" -> Intent(this, ReportsActivity::class.java)
                "Bulk Operations" -> Intent(this, BulkOperationsActivity::class.java)
                "Manual Adjustment" -> Intent(this, ManualAdjustmentActivity::class.java)
                "Conflict Detection" -> Intent(this, ConflictDetectionActivity::class.java)
                "Conflict Resolution" -> Intent(this, ConflictResolutionActivity::class.java)
                "Exam History" -> Intent(this, ExamHistoryActivity::class.java)
                "Notifications" -> Intent(this, NotificationsActivity::class.java)
                "Settings" -> Intent(this, ThemeSettingsActivity::class.java)
                else -> null
            }
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }
            true
        }
        popup.show()
    }
}