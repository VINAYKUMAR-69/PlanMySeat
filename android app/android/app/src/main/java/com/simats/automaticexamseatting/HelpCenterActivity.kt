package com.simats.automaticexamseatting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HelpCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_center)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerCard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        setupFaqs()

        findViewById<View>(R.id.btnContactSupport).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:support@examseating.com")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - Exam Seating System")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }
    }

    private fun setupFaqs() {
        val faqs = listOf(
            "How do I add students to the system?" to "You can add students manually through the 'Add Student' page or upload a CSV file with the student list in the 'CSV Upload' section.",
            "How does the seating allocation algorithm work?" to "Our AI-powered algorithm considers student branches, room capacities, and distance constraints to ensure students from the same branch are not seated next to each other.",
            "Can I manually adjust seat allocations?" to "Yes, after generating a seating plan, you can use the 'Manual Adjustment' tool to move students between seats if needed.",
            "What export formats are supported?" to "The system supports exporting reports in PDF, Excel (.xlsx), and CSV formats for easy printing and sharing.",
            "How do I handle conflicts in seating?" to "The system automatically detects conflicts. You can view and resolve them through the 'Conflict Resolution' dashboard.",
            "Can I add more rooms after allocation?" to "Yes, you can add new rooms anytime. However, to include them in an existing allocation, you will need to re-generate the seating plan.",
            "How do I print room-wise seating charts?" to "Navigate to the 'Reports' section and select 'Room-wise Print' to generate individual charts for each exam room.",
            "Is my data saved automatically?" to "Yes, all changes to students, rooms, and generated seating plans are saved in real-time to the local database."
        )

        val container = findViewById<LinearLayout>(R.id.llFaqContainer)
        faqs.forEach { (question, answer) ->
            val faqView = LayoutInflater.from(this).inflate(R.layout.item_faq, container, false)
            val tvQuestion = faqView.findViewById<TextView>(R.id.tvQuestion)
            val tvAnswer = faqView.findViewById<TextView>(R.id.tvAnswer)
            val ivArrow = faqView.findViewById<ImageView>(R.id.ivArrow)

            tvQuestion.text = question
            tvAnswer.text = answer

            faqView.setOnClickListener {
                if (tvAnswer.visibility == View.GONE) {
                    tvAnswer.visibility = View.VISIBLE
                    ivArrow.rotation = 180f
                } else {
                    tvAnswer.visibility = View.GONE
                    ivArrow.rotation = 0f
                }
            }

            container.addView(faqView)
        }
    }
}
