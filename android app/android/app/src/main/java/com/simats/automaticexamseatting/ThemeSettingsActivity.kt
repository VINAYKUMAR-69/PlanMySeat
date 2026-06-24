package com.simats.automaticexamseatting

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_theme_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        // Spacing logic
        val sbSpacing = findViewById<SeekBar>(R.id.sbSpacing)
        val tvSpacingValue = findViewById<TextView>(R.id.tvSpacingValue)
        
        sbSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSpacingValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Theme logic
        findViewById<LinearLayout>(R.id.llLightTheme).setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(this, "Light theme applied", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.llDarkTheme).setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(this, "Dark theme applied", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.llSystemTheme).setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            Toast.makeText(this, "System theme applied", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnSaveSettings).setOnClickListener {
            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}