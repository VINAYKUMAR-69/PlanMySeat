package com.simats.automaticexamseatting

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicators: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        
        indicators = listOf(
            findViewById(R.id.indicator1),
            findViewById(R.id.indicator2),
            findViewById(R.id.indicator3),
            findViewById(R.id.indicator4)
        )

        val adapter = OnboardingAdapter()
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, view ->
            val isSelected = index == position
            val color = if (isSelected) "#2563EB" else "#E2E8F0"
            view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
            
            val params = view.layoutParams as LinearLayout.LayoutParams
            params.width = if (isSelected) (24 * resources.displayMetrics.density).toInt() else (8 * resources.displayMetrics.density).toInt()
            view.layoutParams = params
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    inner class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = 4

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivTopIcon: ImageView = itemView.findViewById(R.id.ivTopIcon)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            private val llFeatures: LinearLayout = itemView.findViewById(R.id.llFeatures)
            private val bottomSection: View = itemView.findViewById(R.id.bottomSection)
            private val ivBottomIcon: ImageView = itemView.findViewById(R.id.ivBottomIcon)
            private val tvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)
            
            private val btnBackItem: Button = itemView.findViewById(R.id.btnBackItem)
            private val btnNextItem: Button = itemView.findViewById(R.id.btnNextItem)
            private val tvSkipItem: TextView = itemView.findViewById(R.id.tvSkipItem)

            fun bind(position: Int) {
                // Common Click Listeners
                btnBackItem.setOnClickListener {
                    if (viewPager.currentItem > 0) {
                        viewPager.currentItem -= 1
                    }
                }
                
                btnNextItem.setOnClickListener {
                    if (viewPager.currentItem < 3) {
                        viewPager.currentItem += 1
                    } else {
                        navigateToLogin()
                    }
                }
                
                tvSkipItem.setOnClickListener {
                    navigateToLogin()
                }

                when (position) {
                    0 -> {
                        btnBackItem.visibility = View.GONE
                        btnNextItem.text = "Next →"
                        ivTopIcon.setImageResource(R.drawable.ic_person)
                        ivTopIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                        tvTitle.text = "Automated Seating Allocation"
                        tvDescription.text = "Smart algorithms automatically allocate students to exam rooms based on your custom rules and constraints. Save hours of manual work!"
                        addFeatures(listOf("Intelligent student distribution", "Department-wise separation", "Capacity optimization", "Conflict detection"), "#3B82F6")
                        bottomSection.setBackgroundColor(Color.parseColor("#3B82F6"))
                        ivBottomIcon.setImageResource(R.drawable.ic_person)
                        tvPageNumber.text = "01"
                        btnNextItem.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                    }
                    1 -> {
                        btnBackItem.visibility = View.VISIBLE
                        btnNextItem.text = "Next →"
                        ivTopIcon.setImageResource(R.drawable.ic_calendar)
                        ivTopIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#A855F7"))
                        tvTitle.text = "Comprehensive Exam Management"
                        tvDescription.text = "Manage all aspects of your exams in one place - from scheduling to results. Complete control at your fingertips."
                        addFeatures(listOf("Multi-exam scheduling", "Real-time availability tracking", "Automated notifications", "Exam history & archives"), "#A855F7")
                        bottomSection.setBackgroundColor(Color.parseColor("#A855F7"))
                        ivBottomIcon.setImageResource(R.drawable.ic_calendar)
                        tvPageNumber.text = "02"
                        btnNextItem.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#A855F7"))
                    }
                    2 -> {
                        btnBackItem.visibility = View.VISIBLE
                        btnNextItem.text = "Next →"
                        ivTopIcon.setImageResource(R.drawable.ic_reports)
                        ivTopIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                        tvTitle.text = "Advanced Analytics & Reports"
                        tvDescription.text = "Gain insights with powerful analytics and generate detailed reports instantly. Make data-driven decisions effortlessly."
                        addFeatures(listOf("Interactive dashboards", "Custom report builder", "Export to multiple formats", "Performance metrics"), "#10B981")
                        bottomSection.setBackgroundColor(Color.parseColor("#10B981"))
                        ivBottomIcon.setImageResource(R.drawable.ic_reports)
                        tvPageNumber.text = "03"
                        btnNextItem.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                    }
                    3 -> {
                        btnBackItem.visibility = View.VISIBLE
                        btnNextItem.text = "Get Started"
                        ivTopIcon.setImageResource(R.drawable.ic_faculty)
                        ivTopIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#F97316"))
                        tvTitle.text = "Faculty & Room Management"
                        tvDescription.text = "Efficiently manage faculty assignments and room allocations. Optimize resource utilization with ease."
                        addFeatures(listOf("Faculty duty assignment", "Room layout designer", "Availability tracking", "Bulk import/export"), "#F97316")
                        bottomSection.setBackgroundColor(Color.parseColor("#F97316"))
                        ivBottomIcon.setImageResource(R.drawable.ic_faculty)
                        tvPageNumber.text = "04"
                        btnNextItem.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F97316"))
                    }
                }
            }

            private fun addFeatures(features: List<String>, color: String) {
                llFeatures.removeAllViews()
                features.forEach { feature ->
                    val featureView = LayoutInflater.from(itemView.context).inflate(R.layout.item_onboarding_feature, llFeatures, false)
                    featureView.findViewById<TextView>(R.id.tvFeature).text = feature
                    featureView.findViewById<ImageView>(R.id.ivFeatureCheck).imageTintList = ColorStateList.valueOf(Color.parseColor(color))
                    llFeatures.addView(featureView)
                }
            }
        }
    }
}
