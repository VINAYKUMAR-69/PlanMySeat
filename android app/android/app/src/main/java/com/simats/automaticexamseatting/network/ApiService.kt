package com.simats.automaticexamseatting.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Profile Endpoints
    @GET("profile/{email}")
    fun getProfile(@Path("email") email: String): Call<ProfileResponse>

    @PUT("profile/{email}")
    fun updateProfile(
        @Path("email") email: String,
        @Body request: ProfileUpdate
    ): Call<MessageResponse>

    @PUT("change-password/{email}")
    fun changePassword(
        @Path("email") email: String,
        @Body request: PasswordUpdate
    ): Call<MessageResponse>

    @Multipart
    @POST("upload-photo/{email}")
    fun uploadPhoto(
        @Path("email") email: String,
        @Part file: MultipartBody.Part
    ): Call<MessageResponse>

    // Student Endpoints
    @GET("students/{email}")
    fun getStudents(@Path("email") email: String): Call<List<StudentResponse>>

    @POST("students")
    fun addStudent(@Body request: StudentRequest): Call<StudentResponse>

    @PUT("students/{regNo}")
    fun updateStudent(@Path("regNo") regNo: String, @Body request: StudentRequest): Call<StudentResponse>

    @DELETE("students/{student_id}")
    fun deleteStudent(@Path("student_id") studentId: Int): Call<MessageResponse>

    @HTTP(method = "DELETE", path = "students", hasBody = true)
    fun deleteBulkStudents(@Body regNos: List<String>): Call<MessageResponse>

    // Room Endpoints
    @GET("rooms/{email}")
    fun getRooms(@Path("email") email: String): Call<List<RoomResponse>>

    @POST("rooms")
    fun addRoom(@Body request: RoomRequest): Call<RoomResponse>

    @PUT("rooms/{id}")
    fun updateRoom(@Path("id") id: Int, @Body request: RoomRequest): Call<RoomResponse>

    @DELETE("rooms/{id}")
    fun deleteRoom(@Path("id") id: Int): Call<MessageResponse>

    // Seating Plan Endpoints
    @GET("seating-plans/{email}")
    fun getSeatingPlans(@Path("email") email: String): Call<List<RoomWiseReportResponse>> 

    @POST("seating-plans")
    fun addSeatingPlan(@Body request: RoomWiseReportRequest): Call<RoomWiseReportResponse>

    // Final Report Endpoints
    @GET("final-reports/{email}")
    fun getFinalReports(@Path("email") email: String): Call<List<RoomWiseReportResponse>>

    @POST("final-reports")
    fun addFinalReport(@Body request: RoomWiseReportRequest): Call<RoomWiseReportResponse>

    @DELETE("final-reports/{email}")
    fun deleteFinalReports(@Path("email") email: String): Call<MessageResponse>

    @GET("download-report/{email}")
    @Streaming
    fun downloadReport(@Path("email") email: String): Call<ResponseBody>

    @GET("export-students-csv")
    @Streaming
    fun exportStudentsCsv(): Call<ResponseBody>

    // Faculty Endpoints
    @GET("faculties")
    fun getFaculties(): Call<List<FacultyResponse>>

    @POST("faculties")
    fun addFaculty(@Body request: FacultyRequest): Call<MessageResponse>

    @DELETE("faculties/{id}")
    fun deleteFaculty(@Path("id") id: Int): Call<MessageResponse>

    // Notification Endpoints
    @GET("notifications/{email}")
    fun getNotifications(@Path("email") email: String): Call<List<NotificationResponse>>

    @POST("notifications")
    fun addNotification(@Body request: NotificationRequest): Call<AddNotificationResponse>

    @DELETE("notifications/{notification_id}")
    fun deleteNotification(@Path("notification_id") id: Int): Call<MessageResponse>

    @GET("room-wise-reports/{email}")
    fun getRoomWiseReports(@Path("email") email: String): Call<List<RoomWiseReportResponse>>

    // Dashboard Endpoints
    @GET("dashboard/{email}")
    fun getDashboard(@Path("email") email: String): Call<DashboardResponse>

    @GET("dashboard/branch-distribution/{email}")
    fun getBranchDistribution(@Path("email") email: String): Call<BranchDistributionResponse>

    @GET("dashboard/pie-chart/{email}")
    fun getPieChartDistribution(@Path("email") email: String): Call<List<PieChartItem>>

    @GET("dashboard/recent-allocations/{email}")
    fun getRecentAllocations(@Path("email") email: String): Call<List<RecentAllocation>>

    @GET("dashboard/analytics/{email}")
    fun getCompleteAnalytics(@Path("email") email: String): Call<CompleteAnalyticsResponse>

    // Feedback Endpoints
    @POST("feedback")
    fun submitFeedback(@Body request: FeedbackRequest): Call<SubmitFeedbackResponse>

    @GET("feedbacks")
    fun getFeedbacks(): Call<List<FeedbackResponse>>

    @GET("feedback/{id}")
    fun getFeedback(@Path("id") id: Int): Call<FeedbackResponse>

    @PUT("feedback/{id}")
    fun updateFeedback(@Path("id") id: Int, @Body request: FeedbackRequest): Call<MessageResponse>

    @DELETE("feedback/{id}")
    fun deleteFeedback(@Path("id") id: Int): Call<MessageResponse>

    // Bug Report Endpoints
    @Multipart
    @POST("bug-report")
    fun submitBugReport(
        @Part("bug_title") bugTitle: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part("frequency") frequency: RequestBody,
        @Part("description") description: RequestBody,
        @Part("steps_to_reproduce") stepsToReproduce: RequestBody,
        @Part("expected_behavior") expectedBehavior: RequestBody,
        @Part("actual_behavior") actualBehavior: RequestBody,
        @Part screenshot: MultipartBody.Part?
    ): Call<BugReportSubmitResponse>

    @GET("bug-reports")
    fun getBugReports(): Call<List<BugReportResponse>>

    @GET("bug-report/{bug_id}")
    fun getBugReport(@Path("bug_id") id: Int): Call<BugReportResponse>

    @DELETE("bug-report/{bug_id}")
    fun deleteBugReport(@Path("bug_id") id: Int): Call<MessageResponse>

    @Multipart
    @PUT("bug-report/{bug_id}")
    fun updateBugReport(
        @Path("bug_id") id: Int,
        @Part("bug_title") bugTitle: RequestBody,
        @Part("severity") severity: RequestBody,
        @Part("frequency") frequency: RequestBody,
        @Part("description") description: RequestBody,
        @Part("steps_to_reproduce") stepsToReproduce: RequestBody,
        @Part("expected_behavior") expectedBehavior: RequestBody,
        @Part("actual_behavior") actualBehavior: RequestBody,
        @Part screenshot: MultipartBody.Part?
    ): Call<MessageResponse>

    @GET("bug-screenshot/{bug_id}")
    @Streaming
    fun downloadBugScreenshot(@Path("bug_id") id: Int): Call<ResponseBody>

    // Feature Request Endpoints
    @POST("feature-request")
    fun submitFeatureRequest(@Body feature: FeatureRequestCreate): Call<SubmitFeatureRequestResponse>

    @GET("feature-requests")
    fun getFeatureRequests(): Call<List<FeatureRequestResponse>>

    @GET("feature-request/{feature_id}")
    fun getFeatureRequest(@Path("feature_id") id: Int): Call<FeatureRequestResponse>

    @PUT("feature-request/{feature_id}")
    fun updateFeatureRequest(@Path("feature_id") id: Int, @Body feature: FeatureRequestCreate): Call<MessageResponse>

    @DELETE("feature-request/{feature_id}")
    fun deleteFeatureRequest(@Path("feature_id") id: Int): Call<MessageResponse>

    // Exam History Endpoints
    @GET("exam-history")
    fun getExamHistory(): Call<List<ExamHistoryResponse>>

    @GET("exam-history-analytics")
    fun getExamHistoryAnalytics(): Call<ExamHistoryAnalyticsResponse>

    @DELETE("exam-history/{history_id}")
    fun deleteExamHistory(@Path("history_id") id: Int): Call<MessageResponse>

    // Forgot Password Endpoints
    @POST("forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<MessageResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<MessageResponse>

    @POST("reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<MessageResponse>
}
