package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class AiOptimizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_optimization)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        // Functional button for AI Optimization
        findViewById<MaterialButton>(R.id.btnOptimizeAI).setOnClickListener {
            if (DataManager.students.isEmpty()) {
                Toast.makeText(this, "No students found. Please add students first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (DataManager.rooms.isEmpty()) {
                Toast.makeText(this, "No rooms found. Please add rooms first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val btn = it as MaterialButton
            btn.isEnabled = false
            btn.text = "Processing..."
            
            Toast.makeText(this, "AI Intelligent Algorithm is creating the optimal seating plan...", Toast.LENGTH_LONG).show()
            
            // Get the first available exam type from students or use a default
            val examType = DataManager.students.firstOrNull { it.examType.isNotEmpty() }?.examType ?: "General Exam"
            val examDate = "May 25, 2026"
            val examTime = "10:00 AM"

            // Simulate "AI Processing" with a small delay and then navigate
            it.postDelayed({
                val report = DataManager.generateSeatingPlan(examType, examDate, examTime, DataManager.rooms.size)
                
                if (report != null) {
                    val intent = Intent(this, GeneratedSeatingPlanActivity::class.java)
                    intent.putExtra("ROOM_COUNT", DataManager.rooms.size)
                    intent.putExtra("EXAM_TYPE", examType)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to generate plan. Check student/room capacity.", Toast.LENGTH_SHORT).show()
                    btn.isEnabled = true
                    btn.text = "Optimize with AI"
                }
            }, 1500)
        }
    }
}