package com.simats.automaticexamseatting

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class UserManagementActivity : AppCompatActivity() {

    private lateinit var llUserTableList: LinearLayout
    private lateinit var etSearchUser: EditText
    private lateinit var tvTotalUsersStat: TextView
    private lateinit var tvActiveUsersStat: TextView
    private lateinit var tvInactiveUsersStat: TextView
    private lateinit var tvSuspendedUsersStat: TextView
    private lateinit var tvSelectedRole: TextView
    private lateinit var tvSelectedStatus: TextView
    
    private var currentRoleFilter = "All Roles"
    private var currentStatusFilter = "All Status"
    private var filteredUsers: List<AdminUser> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_management)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
        updateStats()
        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        applyFilters()
    }

    private fun initializeViews() {
        llUserTableList = findViewById(R.id.llUserTableList)
        etSearchUser = findViewById(R.id.etSearchUser)
        tvTotalUsersStat = findViewById(R.id.tvTotalUsersStat)
        tvActiveUsersStat = findViewById(R.id.tvActiveUsersStat)
        tvInactiveUsersStat = findViewById(R.id.tvInactiveUsersStat)
        tvSuspendedUsersStat = findViewById(R.id.tvSuspendedUsersStat)
        tvSelectedRole = findViewById(R.id.tvSelectedRole)
        tvSelectedStatus = findViewById(R.id.tvSelectedStatus)
    }

    private fun setupListeners() {
        // Top Navigation
        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }
        
        findViewById<ImageView>(R.id.ivNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        
        findViewById<View>(R.id.cvProfileThumb).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Action Buttons
        findViewById<View>(R.id.btnAddUser).setOnClickListener {
            showAddUserDialog()
        }
        
        findViewById<View>(R.id.btnExportUsers).setOnClickListener {
            exportUsersToCSV()
        }

        // Role Filter Button
        findViewById<View>(R.id.btnRoleFilter).setOnClickListener { view ->
            showRoleFilterMenu(view)
        }

        // Status Filter Button
        findViewById<View>(R.id.btnStatusFilter).setOnClickListener { view ->
            showStatusFilterMenu(view)
        }

        // Search logic
        etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Footer links
        findViewById<TextView>(R.id.tvAbout)?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        findViewById<TextView>(R.id.tvSupport)?.setOnClickListener {
            startActivity(Intent(this, HelpCenterActivity::class.java))
        }
    }

    private fun exportUsersToCSV() {
        if (filteredUsers.isEmpty()) {
            Toast.makeText(this, "No users to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val csvContent = StringBuilder()
            csvContent.append("ID,Name,Email,Phone,Role,Department,Status,Last Active\n")
            
            for (user in filteredUsers) {
                csvContent.append("${user.id},${user.name},${user.email},${user.phone},${user.role},${user.department},${user.status},${user.lastActive}\n")
            }

            val fileName = "user_management_export_${System.currentTimeMillis()}.csv"
            val file = File(cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(csvContent.toString().toByteArray())
            outputStream.close()

            val contentUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_SUBJECT, "User Management Export")
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            startActivity(Intent.createChooser(intent, "Download Exported Data"))
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRoleFilterMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val roles = arrayOf("All Roles", "Admin", "Coordinator", "Viewer", "Staff")
        
        roles.forEach { role ->
            popup.menu.add(role)
        }

        popup.setOnMenuItemClickListener { item ->
            currentRoleFilter = item.title.toString()
            tvSelectedRole.text = currentRoleFilter
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showStatusFilterMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val statuses = arrayOf("All Status", "active", "inactive", "suspended")
        
        statuses.forEach { status ->
            popup.menu.add(status)
        }

        popup.setOnMenuItemClickListener { item ->
            currentStatusFilter = item.title.toString()
            tvSelectedStatus.text = currentStatusFilter.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            applyFilters()
            true
        }
        popup.show()
    }

    private fun applyFilters() {
        val searchQuery = etSearchUser.text.toString()
        filteredUsers = DataManager.systemUsers.filter { user ->
            val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || 
                               user.email.contains(searchQuery, ignoreCase = true)
            
            val matchesRole = currentRoleFilter == "All Roles" || user.role == currentRoleFilter
            
            val matchesStatus = currentStatusFilter == "All Status" || user.status == currentStatusFilter
            
            matchesSearch && matchesRole && matchesStatus
        }
        displayUsers(filteredUsers)
    }

    private fun showAddUserDialog() {
        startActivity(Intent(this, AddNewUserActivity::class.java))
    }

    private fun updateStats() {
        val users = DataManager.systemUsers
        tvTotalUsersStat.text = users.size.toString()
        tvActiveUsersStat.text = users.count { it.status == "active" }.toString()
        tvInactiveUsersStat.text = users.count { it.status == "inactive" }.toString()
        tvSuspendedUsersStat.text = users.count { it.status == "suspended" }.toString()
    }

    private fun displayUsers(list: List<AdminUser>) {
        llUserTableList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (user in list) {
            val itemView = inflater.inflate(R.layout.item_user_table, llUserTableList, false)
            
            itemView.findViewById<TextView>(R.id.tvInitials).text = user.initials
            itemView.findViewById<TextView>(R.id.tvName).text = user.name
            itemView.findViewById<TextView>(R.id.tvUserId).text = "ID: ${user.id}"
            itemView.findViewById<TextView>(R.id.tvEmail).text = user.email
            itemView.findViewById<TextView>(R.id.tvPhone).text = user.phone
            itemView.findViewById<TextView>(R.id.tvRole).text = user.role
            itemView.findViewById<TextView>(R.id.tvDepartment).text = user.department
            itemView.findViewById<TextView>(R.id.tvStatus).text = user.status
            itemView.findViewById<TextView>(R.id.tvLastLogin).text = user.lastActive

            // Role Badge Style
            val tvRole = itemView.findViewById<TextView>(R.id.tvRole)
            when (user.role) {
                "Admin" -> {
                    tvRole.backgroundTintList = ColorStateList.valueOf("#F5F3FF".toColorInt())
                    tvRole.setTextColor("#7C3AED".toColorInt())
                }
                "Coordinator" -> {
                    tvRole.backgroundTintList = ColorStateList.valueOf("#EFF6FF".toColorInt())
                    tvRole.setTextColor("#2563EB".toColorInt())
                }
                "Staff" -> {
                    tvRole.backgroundTintList = ColorStateList.valueOf("#FFF7ED".toColorInt())
                    tvRole.setTextColor("#EA580C".toColorInt())
                }
                "Viewer" -> {
                    tvRole.backgroundTintList = ColorStateList.valueOf("#F1F5F9".toColorInt())
                    tvRole.setTextColor("#475569".toColorInt())
                }
            }

            // Status Badge Style
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (user.status) {
                "active" -> {
                    tvStatus.backgroundTintList = ColorStateList.valueOf("#DCFCE7".toColorInt())
                    tvStatus.setTextColor("#10B981".toColorInt())
                }
                "inactive" -> {
                    tvStatus.backgroundTintList = ColorStateList.valueOf("#F1F5F9".toColorInt())
                    tvStatus.setTextColor("#64748B".toColorInt())
                }
                "suspended" -> {
                    tvStatus.backgroundTintList = ColorStateList.valueOf("#FEE2E2".toColorInt())
                    tvStatus.setTextColor("#DC2626".toColorInt())
                }
            }

            itemView.findViewById<ImageView>(R.id.ivActions).setOnClickListener { view ->
                showUserActionMenu(view, user)
            }

            llUserTableList.addView(itemView)
        }
    }

    private fun showUserActionMenu(anchor: View, user: AdminUser) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("View Profile")
        popup.menu.add("Edit User")
        popup.menu.add("Change Status")
        popup.menu.add("Delete User")

        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(this, "${item.title} for ${user.name}", Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }
}
