package com.simats.automaticexamseatting

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.view.isVisible

object MenuHelper {
    fun showCustomMenu(activity: Activity, anchorView: View) {
        val popupView = activity.layoutInflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.animationStyle = android.R.style.Animation_Dialog
        
        popupView.findViewById<ImageView>(R.id.ivCloseMenu).setOnClickListener { popupWindow.dismiss() }
        
        setupMenuExpansion(popupView)
        setupClickListeners(activity, popupView, popupWindow)

        // anchorView is used to get the window token, but we show at Gravity.START
        popupWindow.showAtLocation(anchorView.rootView, Gravity.START, 0, 0)
    }

    private fun setupMenuExpansion(view: View) {
        val groups = listOf(
            Triple(R.id.menuStudentsParent, R.id.llStudentsSubItems, R.id.ivStudentsArrow),
            Triple(R.id.menuFacultyParent, R.id.llFacultySubItems, R.id.ivFacultyArrow),
            Triple(R.id.menuStudentIssuesParent, R.id.llStudentIssuesSubItems, R.id.ivStudentIssuesArrow),
            Triple(R.id.menuConflictsParent, R.id.llConflictsSubItems, R.id.ivConflictsArrow),
            Triple(R.id.menuRoomsParent, R.id.llRoomsSubItems, R.id.ivRoomsArrow),
            Triple(R.id.menuAdminParent, R.id.llAdminSubItems, R.id.ivAdminArrow),
            Triple(R.id.menuInfoParent, R.id.llInfoSubItems, R.id.ivInfoArrow)
        )
        groups.forEach { (headerId, submenuId, arrowId) ->
            view.findViewById<View>(headerId)?.setOnClickListener {
                val sub = view.findViewById<View>(submenuId)
                val arrow = view.findViewById<ImageView>(arrowId)
                if (sub != null) {
                    val isVisible = sub.isVisible
                    sub.isVisible = !isVisible
                    arrow?.animate()?.rotation(if (isVisible) 270f else 180f)?.setDuration(200)?.start()
                }
            }
        }
    }

    private fun setupClickListeners(activity: Activity, view: View, popupWindow: PopupWindow) {
        val clickListener = View.OnClickListener { v ->
            val intent = when(v.id) {
                R.id.menuDashboard -> Intent(activity, DashboardActivity::class.java)
                
                // Students
                R.id.menuStudentList -> Intent(activity, StudentsActivity::class.java)
                R.id.menuAddStudent -> Intent(activity, AddStudentActivity::class.java)
                R.id.menuCsvUpload -> Intent(activity, CsvUploadActivity::class.java)
                R.id.menuBulkOperations -> Intent(activity, BulkOperationsActivity::class.java)
                
                // Faculty
                R.id.menuFacultyDashboard -> Intent(activity, FacultyDashboardActivity::class.java)
                R.id.menuAllFaculty -> Intent(activity, AllFacultyActivity::class.java)
                R.id.menuImportFaculty -> Intent(activity, ImportFacultyActivity::class.java)
                R.id.menuGeneratePlan -> Intent(activity, GenerateFacultyPlanActivity::class.java)
                
                // Student Issues
                R.id.menuIssuesDashboard -> Intent(activity, StudentIssuesDashboardActivity::class.java)
                R.id.menuAllIssues -> Intent(activity, AllIssuesActivity::class.java)
                R.id.menuReportIssue -> Intent(activity, ReportIssueActivity::class.java)
                R.id.menuArchive -> Intent(activity, ArchivedIssuesActivity::class.java)
                
                // Seating
                R.id.menuSeatAllocation -> Intent(activity, SeatAllocationSetupActivity::class.java)
                R.id.menuSeatingPlan -> Intent(activity, GeneratedSeatingPlanActivity::class.java)
                
                // Conflicts
                R.id.menuConflictDetection -> Intent(activity, ConflictDetectionActivity::class.java)
                R.id.menuConflictResolution -> Intent(activity, ConflictResolutionActivity::class.java)
                
                // Rooms
                R.id.menuRoomList -> Intent(activity, RoomsActivity::class.java)
                R.id.menuAddRoom -> Intent(activity, AddRoomActivity::class.java)
                R.id.menuRoomAvailability -> Intent(activity, RoomAvailabilityActivity::class.java)
                R.id.menuRoomOccupied -> Intent(activity, RoomOccupiedActivity::class.java)
                
                // Reports & Others
                R.id.menuReports -> Intent(activity, ReportsActivity::class.java)
                R.id.menuRoomPrint -> Intent(activity, RoomWiseReportActivity::class.java)
                R.id.menuExamHistory -> Intent(activity, ExamHistoryActivity::class.java)
                R.id.menuNotifications -> Intent(activity, NotificationsActivity::class.java)
                
                // Admin
                R.id.menuUserManagement -> Intent(activity, UserManagementActivity::class.java)
                R.id.menuRoleManagement -> Intent(activity, AdminPanelActivity::class.java)
                R.id.menuSystemLogs -> Intent(activity, SystemLogsActivity::class.java)
                R.id.menuActivityMonitor -> Intent(activity, ActivityMonitorActivity::class.java)
                
                // Settings
                R.id.menuSettings -> Intent(activity, ThemeSettingsActivity::class.java)
                
                // Info and Support
                R.id.menuProfile -> Intent(activity, UserProfileActivity::class.java)
                R.id.menuAbout -> Intent(activity, AboutActivity::class.java)
                R.id.menuHelpCenter -> Intent(activity, HelpCenterActivity::class.java)
                R.id.menuSendFeedback -> Intent(activity, FeedbackActivity::class.java)
                R.id.menuReportBug -> Intent(activity, ReportBugActivity::class.java)
                R.id.menuRequestFeature -> Intent(activity, RequestFeatureActivity::class.java)
                R.id.menuFeedbackHistory -> Intent(activity, FeedbackHistoryActivity::class.java)
                R.id.menuTermsPolicy -> Intent(activity, TermsPrivacyActivity::class.java)
                R.id.menuAppInfo -> Intent(activity, AppInfoActivity::class.java)
                
                else -> null
            }
            
            if (intent != null) {
                // Check if target activity is already current activity to avoid redundant restarts
                if (activity.javaClass != intent.component?.className?.let { Class.forName(it) }) {
                    activity.startActivity(intent)
                }
                popupWindow.dismiss()
            }
        }

        val menuIds = listOf(
            R.id.menuDashboard, R.id.menuStudentList, R.id.menuAddStudent, R.id.menuCsvUpload, R.id.menuBulkOperations,
            R.id.menuFacultyDashboard, R.id.menuAllFaculty, R.id.menuImportFaculty, R.id.menuGeneratePlan,
            R.id.menuIssuesDashboard, R.id.menuAllIssues, R.id.menuReportIssue, R.id.menuArchive,
            R.id.menuSeatAllocation, R.id.menuSeatingPlan,
            R.id.menuConflictDetection, R.id.menuConflictResolution,
            R.id.menuRoomList, R.id.menuAddRoom, R.id.menuRoomAvailability, R.id.menuRoomOccupied,
            R.id.menuReports, R.id.menuRoomPrint, R.id.menuExamHistory, R.id.menuNotifications,
            R.id.menuUserManagement, R.id.menuRoleManagement, R.id.menuSystemLogs, R.id.menuActivityMonitor,
            R.id.menuSettings,
            R.id.menuProfile, R.id.menuAbout, R.id.menuHelpCenter, R.id.menuSendFeedback, R.id.menuReportBug, 
            R.id.menuRequestFeature, R.id.menuFeedbackHistory, R.id.menuTermsPolicy, R.id.menuAppInfo
        )

        menuIds.forEach { id ->
            view.findViewById<View>(id)?.setOnClickListener(clickListener)
        }
    }
}
