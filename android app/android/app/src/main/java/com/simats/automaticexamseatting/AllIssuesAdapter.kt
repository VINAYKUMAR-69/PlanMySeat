package com.simats.automaticexamseatting

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AllIssuesAdapter(
    private var issues: List<FacultyIssue>,
    private val onActionComplete: () -> Unit = {}
) : RecyclerView.Adapter<AllIssuesAdapter.IssueViewHolder>() {

    class IssueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIssueIcon: ImageView = view.findViewById(R.id.ivIssueIcon)
        val flIconBg: FrameLayout = view.findViewById(R.id.flIconBg)
        val tvIssueTitle: TextView = view.findViewById(R.id.tvIssueTitle)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val tvPriorityBadge: TextView = view.findViewById(R.id.tvPriorityBadge)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvFacultyAvatar: TextView = view.findViewById(R.id.tvFacultyAvatar)
        val tvFacultyName: TextView = view.findViewById(R.id.tvFacultyName)
        val tvTimeAgo: TextView = view.findViewById(R.id.tvTimeAgo)
        val tvSemester: TextView = view.findViewById(R.id.tvSemester)
        val tvAssignedTo: TextView = view.findViewById(R.id.tvAssignedTo)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        val ivMenuMore: ImageView = view.findViewById(R.id.ivMenuMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_issue_card, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.tvIssueTitle.text = issue.title
        holder.tvDescription.text = issue.description
        holder.tvFacultyName.text = issue.studentName
        holder.tvTimeAgo.text = issue.date
        holder.tvSemester.text = "${issue.category} Dept"
        holder.tvAssignedTo.text = "Academic Office"
        holder.tvDueDate.text = "May 20, 24"
        
        // Initials for avatar
        holder.tvFacultyAvatar.text = if (issue.studentName.isNotEmpty()) {
            issue.studentName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
        } else "ST"

        // Status styling
        holder.tvStatusBadge.text = issue.status.uppercase()
        when (issue.status) {
            "Open" -> {
                holder.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"))
            }
            "In Progress" -> {
                holder.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                holder.tvStatusBadge.setTextColor(Color.parseColor("#D97706"))
            }
            "Resolved" -> {
                holder.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
                holder.tvStatusBadge.setTextColor(Color.parseColor("#059669"))
            }
        }

        // Priority styling
        holder.tvPriorityBadge.text = issue.priority.uppercase()
        when (issue.priority) {
            "High" -> {
                holder.tvPriorityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
                holder.tvPriorityBadge.setTextColor(Color.parseColor("#1F2937"))
            }
            "Medium" -> {
                holder.tvPriorityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
                holder.tvPriorityBadge.setTextColor(Color.parseColor("#4B5563"))
            }
            "Low" -> {
                holder.tvPriorityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
                holder.tvPriorityBadge.setTextColor(Color.parseColor("#9CA3AF"))
            }
        }

        // Icon and Color based on Category
        val iconRes = when (issue.category) {
            "Academic" -> R.drawable.ic_school
            "Attendance" -> R.drawable.ic_calendar
            "Behavioral" -> R.drawable.ic_error
            "Health" -> R.drawable.ic_person
            "Financial" -> R.drawable.ic_work
            else -> R.drawable.ic_faculty
        }

        val colorHex = when (issue.category) {
            "Academic" -> "#3B82F6"
            "Attendance" -> "#F97316"
            "Behavioral" -> "#EF4444"
            "Health" -> "#10B981"
            "Financial" -> "#8B5CF6"
            else -> "#64748B"
        }
        
        val colorInt = Color.parseColor(colorHex)
        holder.ivIssueIcon.setImageResource(iconRes)
        holder.ivIssueIcon.imageTintList = ColorStateList.valueOf(colorInt)
        holder.flIconBg.backgroundTintList = ColorStateList.valueOf(colorInt).withAlpha(40)
        holder.tvFacultyAvatar.backgroundTintList = ColorStateList.valueOf(colorInt)

        holder.ivMenuMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            if (issue.isArchived) {
                popup.menu.add("Unarchive")
            } else {
                popup.menu.add("Archive")
            }
            popup.menu.add("Delete")
            
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Archive" -> {
                        issue.isArchived = true
                        Toast.makeText(view.context, "Issue archived", Toast.LENGTH_SHORT).show()
                        onActionComplete()
                        true
                    }
                    "Unarchive" -> {
                        issue.isArchived = false
                        Toast.makeText(view.context, "Issue moved to active", Toast.LENGTH_SHORT).show()
                        onActionComplete()
                        true
                    }
                    "Delete" -> {
                        DataManager.facultyIssues.remove(issue)
                        Toast.makeText(view.context, "Issue deleted", Toast.LENGTH_SHORT).show()
                        onActionComplete()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = issues.size

    fun updateData(newIssues: List<FacultyIssue>) {
        issues = newIssues
        notifyDataSetChanged()
    }
}