package com.simats.automaticexamseatting.network

import com.google.gson.annotations.SerializedName

// --- Auth Models ---
data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    @SerializedName("college_organization") val collegeOrganization: String
)

data class RegisterResponse(val message: String? = null)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val message: String,
    @SerializedName("full_name", alternate = ["fullName"]) val full_name: String,
    val email: String,
    val role: String
)

// --- Profile Models ---
data class ProfileResponse(
    @SerializedName("full_name", alternate = ["fullName"]) val fullName: String = "",
    val email: String = "",
    val photo: String? = null,
    val role: String? = null,
    @SerializedName("college_organization", alternate = ["collegeOrganization"]) val collegeOrganization: String? = null,
    val college: String? = null,
    @SerializedName("exams_created", alternate = ["examsCreated"]) val examsCreated: Int = 0,
    @SerializedName("member_since", alternate = ["memberSince"]) val memberSince: String? = null,
    @SerializedName("last_login", alternate = ["lastLogin"]) val lastLogin: String? = null
)

data class ProfileUpdate(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("college_organization") val collegeOrganization: String
)

data class PasswordUpdate(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// --- Dashboard Models ---
data class DashboardResponse(
    @SerializedName("students", alternate = ["total_students", "student_count", "totalStudents", "studentCount"]) val students: Int = 0,
    @SerializedName("rooms", alternate = ["total_rooms", "room_count", "totalRooms", "roomCount"]) val rooms: Int = 0,
    @SerializedName("notifications", alternate = ["total_notifications", "notification_count", "totalNotifications", "notificationCount"]) val notifications: Int = 0,
    @SerializedName("allocated", alternate = ["total_allocated", "total_allocations", "allocated_count", "totalAllocated", "totalAllocations", "allocatedCount"]) val allocated: Int = 0,
    @SerializedName("branches", alternate = ["total_branches", "branch_count", "totalBranches", "branchCount"]) val branches: Int = 0
)

data class BranchDistributionResponse(
    val labels: List<String> = emptyList(),
    val values: List<Int> = emptyList()
)

data class PieChartItem(
    val branch: String? = "Unknown",
    val students: Int = 0,
    val percentage: Double = 0.0
)

data class RecentAllocation(
    @SerializedName("student_name", alternate = ["studentName"]) val studentName: String? = "N/A",
    @SerializedName("reg_no", alternate = ["regNo"]) val regNo: String? = "N/A",
    @SerializedName("room_number", alternate = ["roomNumber"]) val roomNumber: String? = "N/A",
    @SerializedName("seat_no", alternate = ["seatNo"]) val seatNo: Int = 0,
    val branch: String? = "N/A",
    val date: String? = "N/A",
    val status: String? = "N/A"
)

data class UpcomingExamResponse(
    val id: Int = 0,
    @SerializedName("exam_name", alternate = ["examName"]) val examName: String? = "No Exam",
    @SerializedName("exam_date", alternate = ["examDate"]) val examDate: String? = "N/A",
    @SerializedName("exam_time", alternate = ["examTime"]) val examTime: String? = "N/A",
    val department: String? = "N/A"
)

data class CompleteAnalyticsResponse(
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("total_rooms") val totalRooms: Int = 0,
    @SerializedName("total_allocations") val totalAllocations: Int = 0,
    @SerializedName("total_notifications") val totalNotifications: Int = 0,
    @SerializedName("total_exams") val totalExams: Int = 0,
    @SerializedName("branch_distribution") val branchDistribution: BranchDistributionResponse? = BranchDistributionResponse(),
    @SerializedName("pie_chart") val pieChart: List<PieChartItem>? = emptyList(),
    @SerializedName("recent_allocations") val recentAllocations: List<RecentAllocation>? = emptyList(),
    val notifications: List<NotificationResponse>? = emptyList(),
    @SerializedName("upcoming_exams") val upcomingExams: List<UpcomingExamResponse>? = emptyList()
)

// --- Notification Models ---
data class NotificationRequest(
    @SerializedName("user_email") val userEmail: String,
    val title: String,
    val message: String,
    val date: String,
    val time: String,
    val sender: String
)

data class NotificationResponse(
    val id: Int = 0,
    @SerializedName("user_email") val userEmail: String? = null,
    val title: String? = null,
    val message: String? = null,
    val date: String? = null,
    val time: String? = null,
    val sender: String? = null
)

data class AddNotificationResponse(
    val message: String? = null,
    val notification: NotificationResponse? = null
)

data class MessageResponse(val message: String? = null)

// --- Student Models ---
data class StudentRequest(
    @SerializedName("user_email") val userEmail: String,
    val name: String,
    @SerializedName("reg_no") val regNo: String,
    val branch: String,
    val year: String,
    @SerializedName("exam_type") val examType: String? = null
)

data class StudentResponse(
    @SerializedName("id", alternate = ["student_id", "studentId"]) val id: Int = 0,
    @SerializedName("user_email") val userEmail: String? = null,
    val name: String? = "",
    @SerializedName("reg_no") val regNo: String? = "",
    val branch: String? = "",
    val year: String? = "",
    @SerializedName("exam_type") val examType: String? = ""
)

// --- Room Models ---
data class RoomRequest(
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("room_number") val roomNumber: String,
    val capacity: Int,
    val building: String
)

data class RoomResponse(
    @SerializedName("id", alternate = ["room_id", "roomId"]) val id: Int = 0,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("room_number", alternate = ["roomNumber"]) val roomNumber: String = "",
    val capacity: Int = 0,
    val building: String = ""
)

// --- Faculty Models ---
data class FacultyRequest(
    @SerializedName("faculty_id") val facultyId: String,
    val name: String,
    val designation: String,
    val department: String,
    val phone: String,
    val experience: Int,
    val papers: Int = 0,
    val rating: String,
    val status: String
)

data class FacultyResponse(
    val id: Int = 0,
    @SerializedName("faculty_id") val facultyId: String? = null,
    val name: String = "",
    val designation: String = "",
    val department: String = "",
    val phone: String? = null,
    val experience: Int = 0,
    val papers: Int = 0,
    val rating: String = "0.0",
    val status: String = ""
)

// --- Room Wise Report Models ---
data class RoomWiseReportRequest(
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("reg_no") val regNo: String,
    val branch: String,
    @SerializedName("seat_no") val seatNo: Int,
    @SerializedName("room_number") val roomNumber: String,
    val building: String,
    val invigilator: String,
    val subject: String,
    val date: String,
    val time: String,
    @SerializedName("exam_name") val examName: String = "",
    @SerializedName("exam_date") val examDate: String = "",
    @SerializedName("exam_time") val examTime: String = "",
    @SerializedName("student_reg_no") val studentRegNo: String = "",
    @SerializedName("seat_number") val seatNumber: Int = 0
)

data class RoomWiseReportResponse(
    val id: Int = 0,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("reg_no") val regNo: String? = null,
    val branch: String? = null,
    @SerializedName("seat_no") val seatNo: Int = 0,
    @SerializedName("room_number") val roomNumber: String? = null,
    val building: String? = null,
    val invigilator: String? = null,
    val subject: String? = null,
    val date: String? = null,
    val time: String? = null
)

// --- Feedback Models ---
data class FeedbackRequest(
    val name: String,
    val email: String,
    @SerializedName("feedback_type") val feedbackType: String,
    val rating: Int,
    val message: String,
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = ""
)

data class SubmitFeedbackResponse(val message: String? = null)

data class FeedbackResponse(
    val id: Int = 0,
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "",
    val priority: String = "",
    val response: String? = null,
    val date: String? = null
)

// --- Bug Report Models ---
data class BugReportSubmitResponse(val message: String? = null, @SerializedName("bug_id") val bugId: Int? = null)

data class BugReportResponse(
    val id: Int = 0,
    @SerializedName("bug_title") val bugTitle: String = "",
    val severity: String = "",
    val frequency: String = "",
    val description: String = "",
    @SerializedName("steps_to_reproduce") val stepsToReproduce: String = "",
    @SerializedName("expected_behavior") val expectedBehavior: String = "",
    @SerializedName("actual_behavior") val actualBehavior: String = "",
    val screenshot: String? = null,
    val status: String? = "Open",
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Feature Request Models ---
data class FeatureRequestCreate(
    @SerializedName("feature_title") val featureTitle: String,
    val description: String,
    val category: String,
    val priority: String,
    @SerializedName("use_case") val useCase: String,
    @SerializedName("expected_benefit") val expectedBenefit: String,
    val title: String = ""
)

data class SubmitFeatureRequestResponse(val message: String? = null, @SerializedName("feature_id") val featureId: Int? = null)

data class FeatureRequestResponse(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "",
    val priority: String = "",
    @SerializedName("created_at") val createdAt: String? = null
)

// --- Exam History Models ---
data class ExamHistoryResponse(
    val id: Int = 0,
    @SerializedName("file_name") val fileName: String = "",
    @SerializedName("file_type") val fileType: String = "",
    @SerializedName("file_size") val fileSize: String = "",
    val category: String = "",
    @SerializedName("exam_name") val examName: String? = null,
    @SerializedName("exam_date") val examDate: String? = null,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("rooms_used") val roomsUsed: Int = 0,
    val status: String? = "Completed"
)

data class ExamHistoryAnalyticsResponse(
    @SerializedName("total_exports") val totalExports: Int = 0,
    @SerializedName("total_size") val totalSize: String = "",
    @SerializedName("pdf_files") val pdfFiles: Int = 0,
    @SerializedName("csv_excel_files") val csvExcelFiles: Int = 0,
    @SerializedName("storage_used") val storageUsed: String = "",
    @SerializedName("seating_plans") val seatingPlans: Int = 0,
    @SerializedName("student_lists") val studentLists: Int = 0,
    @SerializedName("room_reports") val roomReports: Int = 0,
    val analytics: Int = 0
)

// --- Forgot Password Models ---
data class ForgotPasswordRequest(val email: String)
data class VerifyOtpRequest(val email: String, val otp: String)
data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String
)