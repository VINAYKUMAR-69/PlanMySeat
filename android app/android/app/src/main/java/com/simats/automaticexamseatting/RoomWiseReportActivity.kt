package com.simats.automaticexamseatting

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.RetrofitClient
import com.simats.automaticexamseatting.network.RoomWiseReportResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.OutputStream

class RoomWiseReportActivity : AppCompatActivity() {

    private lateinit var llRoomReportsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var allReports: MutableList<RoomWiseReportResponse> = mutableListOf()
    private var userEmail: String = ""

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
        setContentView(R.layout.activity_room_wise_report)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        llRoomReportsContainer = findViewById(R.id.llRoomReportsContainer)
        
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 50
            }
        }
        llRoomReportsContainer.addView(progressBar)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnDownloadPlans).setOnClickListener {
            if (allReports.isNotEmpty()) {
                exportRoomWisePdf()
            } else {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnPrintAll).setOnClickListener {
            Toast.makeText(this, "Printer connected. Printing all room charts...", Toast.LENGTH_SHORT).show()
        }

        loadData()
        checkNotificationPermission()
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@RoomWiseReportActivity)
            val localReports = db.seatingReportDao().getAllReports()
            
            withContext(Dispatchers.Main) {
                if (localReports.isNotEmpty()) {
                    val mapped = localReports.map { 
                        RoomWiseReportResponse(
                            id = it.id,
                            studentName = it.studentName,
                            regNo = it.regNo,
                            branch = it.branch,
                            seatNo = it.seatNo,
                            roomNumber = it.roomNumber,
                            building = it.building,
                            invigilator = it.invigilator,
                            subject = it.subject,
                            date = it.date,
                            time = it.time
                        )
                    }
                    allReports.clear()
                    allReports.addAll(mapped)
                    displayReports(allReports)
                }
                fetchRoomWiseReports()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun fetchRoomWiseReports() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getRoomWiseReports(userEmail).enqueue(object : Callback<List<RoomWiseReportResponse>> {
            override fun onResponse(
                call: Call<List<RoomWiseReportResponse>>,
                response: Response<List<RoomWiseReportResponse>>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val networkReports = response.body() ?: emptyList()
                    if (networkReports.isNotEmpty()) {
                        allReports.clear()
                        allReports.addAll(networkReports)
                        displayReports(allReports)
                        
                        lifecycleScope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getDatabase(this@RoomWiseReportActivity)
                            val entities = networkReports.map { 
                                SeatingReportEntity(
                                    studentName = it.studentName ?: "",
                                    regNo = it.regNo ?: "",
                                    branch = it.branch ?: "",
                                    year = 0,
                                    seatNo = it.seatNo,
                                    roomNumber = it.roomNumber ?: "",
                                    building = it.building ?: "",
                                    invigilator = it.invigilator ?: "",
                                    subject = it.subject ?: "",
                                    date = it.date ?: "",
                                    time = it.time ?: ""
                                )
                            }
                            db.seatingReportDao().deleteAllReports()
                            db.seatingReportDao().insertReports(entities)
                        }
                    } else if (allReports.isEmpty()) {
                        showEmptyState("No seating reports found.")
                    }
                } else {
                    if (allReports.isEmpty()) {
                        showEmptyState("Failed to fetch reports: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<RoomWiseReportResponse>>, t: Throwable) {
                progressBar.visibility = View.GONE
                if (allReports.isEmpty()) {
                    showEmptyState("Network error: ${t.message}")
                }
            }
        })
    }

    private fun showEmptyState(message: String) {
        llRoomReportsContainer.removeAllViews()
        val emptyTv = TextView(this)
        emptyTv.text = message
        emptyTv.gravity = Gravity.CENTER
        emptyTv.setPadding(0, 100, 0, 0)
        llRoomReportsContainer.addView(emptyTv)
    }

    private fun displayReports(reports: List<RoomWiseReportResponse>) {
        llRoomReportsContainer.removeAllViews()
        
        val reportsByRoom = reports.groupBy { it.roomNumber }

        reportsByRoom.forEach { (roomNumber, roomAllocations) ->
            val firstReport = roomAllocations[0]
            val roomCard = layoutInflater.inflate(R.layout.item_room_wise_card, llRoomReportsContainer, false)
            
            roomCard.findViewById<TextView>(R.id.tvReportRoomName).text = "Room $roomNumber"
            roomCard.findViewById<TextView>(R.id.tvReportBuilding).text = firstReport.building ?: ""
            roomCard.findViewById<TextView>(R.id.tvReportExamType).text = firstReport.subject ?: ""
            roomCard.findViewById<TextView>(R.id.tvReportDateTime).text = "Date: ${firstReport.date ?: ""} • Time: ${firstReport.time ?: ""}"
            
            val tvFacultyName = roomCard.findViewById<TextView>(R.id.tvFacultyName)
            tvFacultyName.text = "Invigilator: ${firstReport.invigilator ?: ""}"
            
            val table = roomCard.findViewById<TableLayout>(R.id.tlReportTable)
            table.removeAllViews()

            // Add Header Row
            val headerRow = TableRow(this)
            headerRow.setBackgroundColor(Color.parseColor("#F1F5F9"))
            headerRow.setPadding(0, 12, 0, 12)
            headerRow.addView(createHeaderCell("S.No"))
            headerRow.addView(createHeaderCell("Seat"))
            headerRow.addView(createHeaderCell("Student Name"))
            headerRow.addView(createHeaderCell("Reg No"))
            headerRow.addView(createHeaderCell("Branch"))
            table.addView(headerRow)

            roomAllocations.forEachIndexed { index, allocation ->
                val row = TableRow(this)
                row.setPadding(0, 12, 0, 12)
                
                val bottomBorder = View(this)
                bottomBorder.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                bottomBorder.setBackgroundColor(Color.parseColor("#E2E8F0"))

                row.addView(createCell("${index + 1}")) 
                row.addView(createCell("${allocation.seatNo}", true)) 
                row.addView(createCell(allocation.studentName ?: ""))
                row.addView(createCell(allocation.regNo ?: ""))
                row.addView(createCell(allocation.branch ?: ""))

                table.addView(row)
                table.addView(bottomBorder)
            }

            roomCard.findViewById<TextView>(R.id.tvReportFooterSummary).text = 
                "Room: $roomNumber | Total Students: ${roomAllocations.size}"

            llRoomReportsContainer.addView(roomCard)
        }
    }

    private fun createHeaderCell(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.parseColor("#475569"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }
    }

    private fun createCell(text: String, isBold: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.parseColor("#1E293B"))
            textSize = 14f
            if (isBold) setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }
    }

    private fun exportRoomWisePdf() {
        if (allReports.isEmpty()) return

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            color = Color.DKGRAY
        }

        val reportsByRoom = allReports.groupBy { it.roomNumber }
        var pageCount = 1

        reportsByRoom.forEach { (roomNumber, roomAllocations) ->
            val first = roomAllocations[0]
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageCount++).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 50f
            canvas.drawText("Room Wise Seating Report - Room $roomNumber", 40f, y, titlePaint)
            y += 25f
            canvas.drawText("Building: ${first.building ?: ""} | Subject: ${first.subject ?: ""}", 40f, y, textPaint)
            y += 15f
            canvas.drawText("Invigilator: ${first.invigilator ?: ""}", 40f, y, textPaint.apply { isFakeBoldText = true })
            textPaint.isFakeBoldText = false
            y += 15f
            canvas.drawText("Date: ${first.date ?: ""} | Time: ${first.time ?: ""}", 40f, y, textPaint)
            y += 30f

            // Table Header
            canvas.drawText("S.No", 40f, y, headerPaint)
            canvas.drawText("Seat", 80f, y, headerPaint)
            canvas.drawText("Student Name", 130f, y, headerPaint)
            canvas.drawText("Reg No", 320f, y, headerPaint)
            canvas.drawText("Branch", 450f, y, headerPaint)
            
            y += 10f
            canvas.drawLine(40f, y, 555f, y, paint.apply { strokeWidth = 1f; color = Color.LTGRAY })
            y += 20f

            roomAllocations.forEachIndexed { index, allocation ->
                if (y > 780) return@forEachIndexed

                canvas.drawText("${index + 1}", 40f, y, textPaint)
                canvas.drawText("${allocation.seatNo}", 80f, y, textPaint.apply { color = Color.BLUE; isFakeBoldText = true })
                textPaint.color = Color.BLACK; textPaint.isFakeBoldText = false
                canvas.drawText(allocation.studentName ?: "", 130f, y, textPaint)
                canvas.drawText(allocation.regNo ?: "", 320f, y, textPaint)
                canvas.drawText(allocation.branch ?: "", 450f, y, textPaint)

                y += 20f
            }

            canvas.drawText("Total Students in Room $roomNumber: ${roomAllocations.size}", 40f, 810f, headerPaint)
            pdfDocument.finishPage(page)
        }

        val fileName = "RoomWise_Report_${System.currentTimeMillis()}.pdf"
        saveFileToDownloads(fileName, "application/pdf") { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }

    private fun saveFileToDownloads(fileName: String, mimeType: String, writeAction: (OutputStream) -> Unit) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    writeAction(outputStream)
                    Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
