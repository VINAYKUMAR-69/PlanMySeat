package com.simats.automaticexamseatting

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.automaticexamseatting.network.FacultyResponse
import com.simats.automaticexamseatting.network.MessageResponse
import com.simats.automaticexamseatting.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class AllFacultyActivity : AppCompatActivity() {

    private lateinit var rvFaculty: RecyclerView
    private lateinit var adapter: FacultyAdapter
    private lateinit var etSearch: EditText
    private var fullFacultyList = mutableListOf<FacultyMember>()
    private var filteredList = mutableListOf<FacultyMember>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_faculty)

        val headerCard = findViewById<View>(R.id.headerCard)
        ViewCompat.setOnApplyWindowInsetsListener(headerCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btnAddFaculty).setOnClickListener {
            startActivity(Intent(this, AddFacultyActivity::class.java))
        }

        etSearch = findViewById(R.id.etSearch)
        setupSearch()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchFaculties()
    }

    private fun fetchFaculties() {
        RetrofitClient.instance.getFaculties().enqueue(object : Callback<List<FacultyResponse>> {
            override fun onResponse(call: Call<List<FacultyResponse>>, response: Response<List<FacultyResponse>>) {
                if (response.isSuccessful) {
                    val remoteFaculties = response.body() ?: emptyList()
                    
                    // Update DataManager
                    DataManager.facultyMembers.clear()
                    remoteFaculties.forEach { f ->
                        val initials = f.name.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
                        val ratingValue = f.rating.toDoubleOrNull() ?: 0.0
                        
                        val faculty = FacultyMember(
                            id = f.id,
                            name = f.name,
                            designation = f.designation,
                            department = f.department,
                            status = f.status,
                            initials = initials,
                            score = ratingValue * 20.0,
                            phone = f.phone ?: "",
                            experience = f.experience
                        )
                        DataManager.facultyMembers.add(faculty)
                    }
                    
                    fullFacultyList.clear()
                    fullFacultyList.addAll(DataManager.facultyMembers)
                    filter(etSearch.text.toString())
                    updateStats()
                } else {
                    fullFacultyList.clear()
                    fullFacultyList.addAll(DataManager.facultyMembers)
                    filter(etSearch.text.toString())
                    updateStats()
                }
            }

            override fun onFailure(call: Call<List<FacultyResponse>>, t: Throwable) {
                Toast.makeText(this@AllFacultyActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                fullFacultyList.clear()
                fullFacultyList.addAll(DataManager.facultyMembers)
                filter(etSearch.text.toString())
                updateStats()
            }
        })
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(text: String) {
        filteredList.clear()
        if (text.isEmpty()) {
            filteredList.addAll(fullFacultyList)
        } else {
            val query = text.lowercase(Locale.getDefault())
            for (item in fullFacultyList) {
                if (item.name.lowercase(Locale.getDefault()).contains(query) ||
                    item.department.lowercase(Locale.getDefault()).contains(query) ||
                    item.designation.lowercase(Locale.getDefault()).contains(query)) {
                    filteredList.add(item)
                }
            }
        }
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateStats() {
        findViewById<TextView>(R.id.tvActiveCount).text = fullFacultyList.count { it.status == "Active" || it.status == "Confirmed" }.toString()
        findViewById<TextView>(R.id.tvAssignedCount).text = fullFacultyList.count { it.roomsAssigned > 0 }.toString()
        findViewById<TextView>(R.id.tvLeaveCount).text = fullFacultyList.count { it.status == "On Leave" }.toString()
    }

    private fun setupRecyclerView() {
        rvFaculty = findViewById(R.id.rvFaculty)
        rvFaculty.layoutManager = LinearLayoutManager(this)
        adapter = FacultyAdapter(filteredList)
        rvFaculty.adapter = adapter
    }

    private fun showDeleteConfirmation(faculty: FacultyMember) {
        AlertDialog.Builder(this)
            .setTitle("Delete Faculty")
            .setMessage("Are you sure you want to delete ${faculty.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFacultyFromServer(faculty)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFacultyFromServer(faculty: FacultyMember) {
        RetrofitClient.instance.deleteFaculty(faculty.id).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AllFacultyActivity, "Faculty deleted", Toast.LENGTH_SHORT).show()
                    fetchFaculties()
                } else {
                    Toast.makeText(this@AllFacultyActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@AllFacultyActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class FacultyAdapter(private val facultyList: List<FacultyMember>) :
        RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder>() {

        inner class FacultyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvInitials: TextView = view.findViewById(R.id.tvInitials)
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvDesignation: TextView = view.findViewById(R.id.tvDesignation)
            val tvId: TextView = view.findViewById(R.id.tvId)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvDept: TextView = view.findViewById(R.id.tvDept)
            val tvExp: TextView = view.findViewById(R.id.tvExp)
            val tvPapers: TextView = view.findViewById(R.id.tvPapers)
            val tvRating: TextView = view.findViewById(R.id.tvRating)
            val btnViewDetails: View = view.findViewById(R.id.btnViewDetails)
            val btnViewSchedule: View = view.findViewById(R.id.btnViewSchedule)
            val cvAvatar: CardView = view.findViewById(R.id.cvAvatar)
            val ivDelete: ImageView = view.findViewById(R.id.ivDeleteFaculty)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faculty_member, parent, false)
            return FacultyViewHolder(view)
        }

        override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
            val faculty = facultyList[position]
            holder.tvInitials.text = faculty.initials
            holder.tvName.text = faculty.name
            holder.tvDesignation.text = faculty.designation
            holder.tvId.text = "ID: FAC00${faculty.id}"
            holder.tvStatus.text = faculty.status
            holder.tvDept.text = faculty.department
            
            holder.tvExp.text = faculty.experience.toString()
            holder.tvPapers.text = "0"
            holder.tvRating.text = String.format("%.1f", faculty.score / 20.0)

            val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6")
            holder.cvAvatar.setCardBackgroundColor(android.graphics.Color.parseColor(colors[position % colors.size]))

            holder.btnViewDetails.setOnClickListener {
                Toast.makeText(it.context, "Viewing details for ${faculty.name}", Toast.LENGTH_SHORT).show()
            }
            holder.btnViewSchedule.setOnClickListener {
                Toast.makeText(it.context, "Viewing schedule for ${faculty.name}", Toast.LENGTH_SHORT).show()
            }
            holder.ivDelete.setOnClickListener {
                showDeleteConfirmation(faculty)
            }
        }

        override fun getItemCount() = facultyList.size
    }
}
