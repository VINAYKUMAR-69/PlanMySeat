package com.simats.automaticexamseatting

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import androidx.cardview.widget.CardView

class ConflictResolutionActivity : AppCompatActivity() {

    private var totalConflicts = 4
    private var highPriority = 1
    private var resolved = 0
    private var autoFixable = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conflict_resolution)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<MaterialButton>(R.id.btnAutoFixAll).setOnClickListener {
            if (autoFixable > 0) {
                resolved += autoFixable
                totalConflicts -= autoFixable
                autoFixable = 0
                highPriority = 0
                updateUI()
                
                findViewById<CardView>(R.id.cvConflict1).visibility = View.GONE
                findViewById<CardView>(R.id.cvConflict2).visibility = View.GONE
                findViewById<CardView>(R.id.cvConflict4).visibility = View.GONE
                
                Toast.makeText(this, "All same-branch and year conflicts resolved automatically", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No more auto-fixable conflicts", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnResolve1).setOnClickListener {
            if (totalConflicts > 0) {
                resolved++
                totalConflicts--
                highPriority = 0
                updateUI()
                findViewById<CardView>(R.id.cvConflict1).visibility = View.GONE
                Toast.makeText(this, "Resolved: CSE students separated in Room A-101", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnResolve2).setOnClickListener {
            if (totalConflicts > 0) {
                resolved++
                totalConflicts--
                autoFixable--
                updateUI()
                findViewById<CardView>(R.id.cvConflict2).visibility = View.GONE
                Toast.makeText(this, "Resolved: Year-based spacing fixed in Room A-102", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnResolve4).setOnClickListener {
            if (totalConflicts > 0) {
                resolved++
                totalConflicts--
                autoFixable--
                updateUI()
                findViewById<CardView>(R.id.cvConflict4).visibility = View.GONE
                Toast.makeText(this, "Resolved: ECE students separated in Room B-202", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Opening capacity review for Room B-201", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.tvTotalConflicts).text = totalConflicts.toString()
        findViewById<TextView>(R.id.tvHighPriority).text = highPriority.toString()
        findViewById<TextView>(R.id.tvResolved).text = resolved.toString()
        findViewById<TextView>(R.id.tvAutoFixable).text = autoFixable.toString()
    }
}