package com.simats.automaticexamseatting

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.simats.automaticexamseatting.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.OutputStream

class ExportDataActivity : AppCompatActivity() {

    private var exportType: String? = null
    private lateinit var progressBar: ProgressBar

    companion object {
        const val EXTRA_EXPORT_TYPE = "export_type"
        const val TYPE_STUDENTS = "students"
        const val TYPE_ROOMS = "rooms"
        const val TYPE_SEATING_PLAN = "seating_plan"
        const val TYPE_REPORTS = "reports"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_export_data)

        exportType = intent.getStringExtra(EXTRA_EXPORT_TYPE)
        progressBar = findViewById(R.id.pbExportLoading)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvExportDescription).text = when(exportType) {
            TYPE_STUDENTS -> "Exporting Student List"
            TYPE_ROOMS -> "Exporting Room Details"
            TYPE_SEATING_PLAN -> "Exporting Seating Plan"
            TYPE_REPORTS -> "Exporting Seating Reports"
            else -> "Exporting Application Data"
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.llCsvExport).setOnClickListener {
            prepareAndExport("CSV")
        }

        findViewById<LinearLayout>(R.id.llExcelExport).setOnClickListener {
            prepareAndExport("Excel")
        }

        findViewById<LinearLayout>(R.id.llPdfExport).setOnClickListener {
            prepareAndExport("PDF")
        }

        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun prepareAndExport(format: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "") ?: ""

        when (exportType) {
            TYPE_STUDENTS -> if (DataManager.students.isEmpty()) fetchStudents(userEmail, format) else startExport(format)
            TYPE_ROOMS -> if (DataManager.rooms.isEmpty() || DataManager.rooms[0].id == -1) fetchRooms(userEmail, format) else startExport(format)
            TYPE_SEATING_PLAN, TYPE_REPORTS -> {
                if (DataManager.currentExam == null) fetchLatestReports(userEmail, format) else startExport(format)
            }
            else -> startExport(format)
        }
    }

    private fun fetchStudents(userEmail: String, format: String) {
        progressBar.isVisible = true
        RetrofitClient.instance.getStudents(userEmail).enqueue(object : Callback<List<StudentResponse>> {
            override fun onResponse(call: Call<List<StudentResponse>>, response: Response<List<StudentResponse>>) {
                progressBar.isVisible = false
                if (response.isSuccessful) {
                    val serverStudents = response.body() ?: emptyList()
                    DataManager.students.clear()
                    serverStudents.forEach { s ->
                        val examType = s.examType ?: "Model"
                        val yearInt = s.year?.toIntOrNull() ?: 1
                        DataManager.students.add(Student(s.name ?: "", s.regNo ?: "", s.branch ?: "", yearInt, examType))
                    }
                    startExport(format)
                } else {
                    Toast.makeText(this@ExportDataActivity, "Failed to fetch students", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<StudentResponse>>, t: Throwable) {
                progressBar.isVisible = false
                Toast.makeText(this@ExportDataActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRooms(userEmail: String, format: String) {
        progressBar.isVisible = true
        RetrofitClient.instance.getRooms(userEmail).enqueue(object : Callback<List<RoomResponse>> {
            override fun onResponse(call: Call<List<RoomResponse>>, response: Response<List<RoomResponse>>) {
                progressBar.isVisible = false
                if (response.isSuccessful) {
                    val serverRooms = response.body() ?: emptyList()
                    DataManager.rooms.clear()
                    serverRooms.forEach { r ->
                        DataManager.rooms.add(RoomDataObj(r.roomNumber, r.building, r.capacity, number = r.roomNumber, id = r.id))
                    }
                    startExport(format)
                }
            }
            override fun onFailure(call: Call<List<RoomResponse>>, t: Throwable) {
                progressBar.isVisible = false
            }
        })
    }

    private fun fetchLatestReports(userEmail: String, format: String) {
        progressBar.isVisible = true
        RetrofitClient.instance.getFinalReports(userEmail).enqueue(object : Callback<List<RoomWiseReportResponse>> {
            override fun onResponse(call: Call<List<RoomWiseReportResponse>>, response: Response<List<RoomWiseReportResponse>>) {
                progressBar.isVisible = false
                if (response.isSuccessful) {
                    val networkData = response.body() ?: emptyList()
                    if (networkData.isNotEmpty()) {
                        val first = networkData[0]
                        val allocations = networkData.map { 
                            Allocation(it.studentName ?: "", it.regNo ?: "", it.branch ?: "", 0, it.roomNumber ?: "", it.subject ?: "", it.invigilator, it.seatNo, it.building ?: "", it.date ?: "", it.time ?: "")
                        }
                        DataManager.currentExam = SeatingReport(first.subject ?: "", first.date ?: "", first.time ?: "", networkData.map { it.roomNumber }.distinct().size, networkData.size, allocations)
                        startExport(format)
                    } else {
                        Toast.makeText(this@ExportDataActivity, "No seating data found on server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<RoomWiseReportResponse>>, t: Throwable) {
                progressBar.isVisible = false
            }
        })
    }

    private fun startExport(format: String) {
        val fileName = "Export_${exportType ?: "Data"}_${System.currentTimeMillis()}"
        
        when (format) {
            "CSV" -> exportCsv(fileName)
            "Excel" -> exportExcel(fileName)
            "PDF" -> exportPdf(fileName)
        }
        
        // Add to history
        val category = when(exportType) {
            TYPE_STUDENTS -> "students"
            TYPE_ROOMS -> "rooms"
            TYPE_SEATING_PLAN, TYPE_REPORTS -> "seating"
            else -> "analytics"
        }
        val extension = when(format) {
            "CSV" -> ".csv"
            "Excel" -> ".csv" // Using .csv for Excel export for better compatibility with simpler writing methods
            "PDF" -> ".pdf"
            else -> ""
        }
        DataManager.exports.add(0, ExportRecord("$fileName$extension", format, category, (100..2500).random().toDouble()))
    }

    private fun exportCsv(fileName: String) {
        val content = generateTextContent()
        saveFileToDownloads("$fileName.csv", "text/csv") { out ->
            out.write(content.toByteArray())
        }
    }

    private fun exportExcel(fileName: String) {
        val content = generateTextContent()
        // Many mobile viewers open .csv better than pseudo-excel files. 
        // We use Excel MIME type to suggest the app, but .csv for reliability.
        saveFileToDownloads("$fileName.csv", "application/vnd.ms-excel") { out ->
            out.write(content.toByteArray())
        }
    }

    private fun exportPdf(fileName: String) {
        val pdfDocument = PdfDocument()
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50f

        canvas.drawText("Data Export Report - ${exportType?.uppercase() ?: "DATA"}", 40f, y, headerPaint)
        y += 40f

        fun checkNewPage(needed: Float) {
            if (y + needed > 800) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
        }

        when(exportType) {
            TYPE_STUDENTS -> {
                canvas.drawText("Student List", 40f, y, Paint(textPaint).apply { isFakeBoldText = true })
                y += 25f
                DataManager.students.forEach {
                    checkNewPage(20f)
                    canvas.drawText("${it.name} (${it.regNo}) - ${it.branch}", 60f, y, textPaint)
                    y += 20f
                }
            }
            TYPE_ROOMS -> {
                canvas.drawText("Room Details", 40f, y, Paint(textPaint).apply { isFakeBoldText = true })
                y += 25f
                DataManager.rooms.forEach {
                    checkNewPage(20f)
                    canvas.drawText("Room ${it.number} - Capacity: ${it.capacity} (${it.building})", 60f, y, textPaint)
                    y += 20f
                }
            }
            TYPE_SEATING_PLAN, TYPE_REPORTS -> {
                val current = DataManager.currentExam
                if (current != null) {
                    canvas.drawText("Exam Seating Plan: ${current.examType}", 40f, y, Paint(textPaint).apply { isFakeBoldText = true })
                    y += 20f
                    canvas.drawText("Date: ${current.examDate} | Time: ${current.examTime}", 40f, y, textPaint)
                    y += 30f
                    
                    current.allocations.forEach {
                        checkNewPage(20f)
                        canvas.drawText("${it.regNo} - ${it.studentName} | Room: ${it.roomNumber} | Seat: ${it.seatNo}", 60f, y, textPaint)
                        y += 20f
                    }
                }
            }
        }

        pdfDocument.finishPage(page)
        saveFileToDownloads("$fileName.pdf", "application/pdf") { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }

    private fun generateTextContent(): String {
        val sb = StringBuilder()
        when (exportType) {
            TYPE_STUDENTS -> {
                sb.append("Name,Registration Number,Branch,Year,Exam Type\n")
                DataManager.students.forEach {
                    sb.append("${it.name},${it.regNo},${it.branch},${it.year},${it.examType}\n")
                }
            }
            TYPE_ROOMS -> {
                sb.append("Room Number,Building,Capacity\n")
                DataManager.rooms.forEach {
                    sb.append("${it.number},${it.building},${it.capacity}\n")
                }
            }
            TYPE_SEATING_PLAN, TYPE_REPORTS -> {
                val current = DataManager.currentExam
                if (current != null) {
                    sb.append("Subject,Date,Time,Student Name,Reg No,Branch,Room,Seat,Invigilator,Building\n")
                    current.allocations.forEach {
                        sb.append("${current.examType},${current.examDate},${current.examTime},${it.studentName},${it.regNo},${it.branch},${it.roomNumber},${it.seatNo},${it.assignedFaculty ?: "N/A"},${it.building}\n")
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun saveFileToDownloads(name: String, mimeType: String, writer: (OutputStream) -> Unit) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    writer(outputStream)
                    Toast.makeText(this, "File saved to Downloads: $name", Toast.LENGTH_LONG).show()
                    showDownloadNotification(name, it)
                }
            }
        } catch (e: Exception) {
            Log.e("Export", "Error saving file", e)
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDownloadNotification(fileName: String, uri: Uri) {
        val channelId = "exports_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Data Exports", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, contentResolver.getType(uri))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Export Complete")
            .setContentText("File saved: $fileName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
