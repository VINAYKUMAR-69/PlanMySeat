package com.simats.automaticexamseatting

import android.util.Log
import com.simats.automaticexamseatting.network.AddNotificationResponse
import com.simats.automaticexamseatting.network.NotificationRequest
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Classes
data class Notification(val id: Int, val title: String, val message: String, val time: String, var isRead: Boolean)
data class ExportRecord(val fileName: String, val fileType: String, val category: String, val sizeKB: Double)
data class Student(val name: String, val regNo: String, val branch: String, val year: Int, val examType: String = "", val id: Int = 0)
data class RoomDataObj(val name: String, val building: String, val capacity: Int, var occupancy: Int = 0, val number: String = "", var assignedFaculty: String? = null, var id: Int = -1)
data class College(val name: String, val adminCount: Int, val studentCount: Int, val status: String = "active")
data class Administrator(val name: String, val email: String, val college: String)
data class AdminUser(val id: Int, val name: String, val email: String, val phone: String, val role: String, val department: String, val status: String, val lastActive: String, val initials: String)
data class SystemLog(val id: Int, val action: String, val status: String, val category: String, val description: String, val user: String, val timestamp: String, val iconType: String)
data class FacultyIssue(val id: Int, val title: String, val studentName: String, val regNo: String, val category: String, val status: String, val priority: String, val date: String, val description: String, var isArchived: Boolean = false)
data class FeedbackItem(val id: Int, val type: String, val title: String, val description: String, val status: String, val priority: String, val response: String, val date: String)
data class Allocation(val studentName: String, val regNo: String, val branch: String, val year: Int, val roomNumber: String, val examType: String = "", var assignedFaculty: String? = null, var seatNo: Int = 0, val building: String = "", val date: String = "", val time: String = "")
data class SeatingReport(val examType: String, val examDate: String, val examTime: String, val roomsUsed: Int, val totalStudents: Int, val allocations: List<Allocation>)
data class FacultyMember(val id: Int, val name: String, val designation: String, val department: String, val status: String, var roomsAssigned: Int = 0, val initials: String = "", val score: Double = 0.0, val sessions: Int = 0, val email: String = "", val phone: String = "", val qualification: String = "", val experience: Int = 0, val dateOfJoining: String = "", val specialization: String = "", val address: String = "", var assignedExamType: String = "")
data class PageVisit(val page: String, val visits: Int, val color: String)
data class ActiveUserSession(val name: String, val initials: String, val page: String, val device: String, val browser: String, val loginTime: String, val lastSeen: String, val actions: Int)

object DataManager {
    // Initialized as empty lists to avoid showing stale mock data on Dashboard
    val notifications = mutableListOf<Notification>()
    val exports = mutableListOf<ExportRecord>()
    val students = mutableListOf<Student>()
    val rooms = mutableListOf<RoomDataObj>()
    val colleges = mutableListOf<College>()
    val admins = mutableListOf<Administrator>()
    val systemUsers = mutableListOf<AdminUser>()
    val systemLogs = mutableListOf<SystemLog>()
    val facultyIssues = mutableListOf<FacultyIssue>()
    val facultyMembers = mutableListOf<FacultyMember>()
    val feedbackHistory = mutableListOf<FeedbackItem>()
    val reports = mutableListOf<SeatingReport>()
    var currentExam: SeatingReport? = null

    val mostVisitedPages = mutableListOf<PageVisit>()
    val activityTimeline = mutableListOf<Float>()
    val issuesTrendData = mutableListOf<Float>()
    
    val deviceDistribution = mutableMapOf<String, Int>()
    val browserDistribution = mutableMapOf<String, Int>()
    val activeUsersList = mutableListOf<ActiveUserSession>()

    fun addNotification(userEmail: String, title: String, message: String, sender: String = "System") {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        
        val newId = if (notifications.isEmpty()) 1 else notifications.maxOf { it.id } + 1
        notifications.add(0, Notification(newId, title, message, "Just now", false))

        val request = NotificationRequest(userEmail, title, message, currentDate, currentTime, sender)
        RetrofitClient.instance.addNotification(request).enqueue(object : Callback<AddNotificationResponse> {
            override fun onResponse(call: Call<AddNotificationResponse>, response: Response<AddNotificationResponse>) {
                if (response.isSuccessful) Log.d("DataManager", "Notification synced")
            }
            override fun onFailure(call: Call<AddNotificationResponse>, t: Throwable) {
                Log.e("DataManager", "Sync error: ${t.message}")
            }
        })
    }

    fun getUniqueExamTypes(): List<String> = students.map { it.examType }.filter { it.isNotEmpty() }.distinct()

    fun generateSeatingPlan(examType: String, examDate: String, examTime: String, roomCount: Int, useAllStudents: Boolean = false): SeatingReport? {
        if (rooms.isEmpty()) {
            Log.e("DataManager", "Cannot generate plan: No rooms available")
            return null
        }
        
        val selectedRooms = if (roomCount <= 0) rooms else rooms.take(roomCount)
        val studentsList = if (useAllStudents) {
            students.toMutableList()
        } else {
            students.filter { it.examType.equals(examType, ignoreCase = true) }.toMutableList()
        }
        
        if (studentsList.isEmpty()) {
            Log.e("DataManager", "Cannot generate plan: No students found for $examType (useAll=$useAllStudents)")
            return null
        }
        
        val finalAllocations = mutableListOf<Allocation>()
        var studentIndex = 0
        
        selectedRooms.forEach { room ->
            var currentRoomOccupancy = 0
            for (i in 1..room.capacity) {
                if (studentIndex < studentsList.size) {
                    val student = studentsList[studentIndex]
                    finalAllocations.add(Allocation(student.name, student.regNo, student.branch, student.year, room.number, examType, null, i, room.building, examDate, examTime))
                    studentIndex++
                    currentRoomOccupancy++
                } else break
            }
            room.occupancy = currentRoomOccupancy
        }
        
        if (finalAllocations.isEmpty()) {
            Log.e("DataManager", "Cannot generate plan: No allocations made")
            return null
        }

        val report = SeatingReport(examType, examDate, examTime, selectedRooms.size, finalAllocations.size, finalAllocations)
        reports.add(report)
        currentExam = report
        return report
    }

    fun assignFacultyToRooms() {
        val exam = currentExam ?: return
        val activeFaculty = facultyMembers.filter { it.status == "Active" || it.status == "Confirmed" }
        
        val uniqueRooms = exam.allocations.map { it.roomNumber }.distinct()
        
        val assignmentMap = mutableMapOf<String, String>()
        uniqueRooms.forEachIndexed { index, roomNumber ->
            if (index < activeFaculty.size) {
                assignmentMap[roomNumber] = activeFaculty[index].name
            }
        }
        
        exam.allocations.forEach { allocation ->
            allocation.assignedFaculty = assignmentMap[allocation.roomNumber]
        }
    }
}
