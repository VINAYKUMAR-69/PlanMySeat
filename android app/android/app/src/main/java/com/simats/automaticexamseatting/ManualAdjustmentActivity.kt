package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ManualAdjustmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_adjustment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinnerRoom = findViewById<Spinner>(R.id.spinnerRoom)
        val rooms = arrayOf("A-101 (Block A)", "B-301 (Block B)", "C-301 (Block C)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rooms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoom.adapter = adapter

        findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            MenuHelper.showCustomMenu(this, it)
        }

        findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
            Toast.makeText(this, "Resetting layout...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnAutoOptimize).setOnClickListener {
            Toast.makeText(this, "Optimizing room...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnSortByBranch).setOnClickListener {
            Toast.makeText(this, "Sorting by branch...", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnCheckConflicts).setOnClickListener {
            startActivity(Intent(this, ConflictDetectionActivity::class.java))
        }
    }
}