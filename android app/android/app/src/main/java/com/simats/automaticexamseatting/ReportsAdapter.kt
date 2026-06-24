package com.simats.automaticexamseatting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.simats.automaticexamseatting.network.RoomWiseReportResponse

class ReportsAdapter(
    private var reports: List<RoomWiseReportResponse>,
    private val onActionClick: (ActionType) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ActionType { PREVIEW, ROOM_WISE, EXCEL, PDF, DELETE }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private var totalStudents = 0
    private var roomsUsed = 0
    private var branches = 0
    private var examType = "N/A"

    fun setStats(total: Int, rooms: Int, branchCount: Int, type: String) {
        this.totalStudents = total
        this.roomsUsed = rooms
        this.branches = branchCount
        this.examType = type
        notifyItemChanged(0)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSummary: TextView = view.findViewById(R.id.tvReportSummaryHeader)
        val tvTotalStudents: TextView = view.findViewById(R.id.tvTotalStudentsHeader)
        val tvRoomsUsed: TextView = view.findViewById(R.id.tvRoomsUsedHeader)
        val tvBranches: TextView = view.findViewById(R.id.tvBranchesHeader)
        val tvExamType: TextView = view.findViewById(R.id.tvExamTypeHeader)
        
        val btnPreview: MaterialButton = view.findViewById(R.id.btnPreview)
        val btnRoomWise: MaterialButton = view.findViewById(R.id.btnRoomWise)
        val btnExcel: MaterialButton = view.findViewById(R.id.btnExcel)
        val btnPrint: MaterialButton = view.findViewById(R.id.btnPrint)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDeleteReport)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReportStudentName)
        val tvRegNo: TextView = view.findViewById(R.id.tvReportRegNo)
        val tvBranch: TextView = view.findViewById(R.id.tvReportBranch)
        val tvRoom: TextView = view.findViewById(R.id.tvReportRoom)
        val tvBuilding: TextView = view.findViewById(R.id.tvReportBuilding)
        val tvDate: TextView = view.findViewById(R.id.tvReportDate)
        val tvTime: TextView = view.findViewById(R.id.tvReportTime)
        val tvFaculty: TextView = view.findViewById(R.id.tvReportFaculty)
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_HEADER else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_allocation, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.tvTotalStudents.text = totalStudents.toString()
            holder.tvRoomsUsed.text = roomsUsed.toString()
            holder.tvBranches.text = branches.toString()
            holder.tvExamType.text = examType
            holder.tvSummary.text = "Total $totalStudents Students Allocated"
            
            holder.btnPreview.setOnClickListener { onActionClick(ActionType.PREVIEW) }
            holder.btnRoomWise.setOnClickListener { onActionClick(ActionType.ROOM_WISE) }
            holder.btnExcel.setOnClickListener { onActionClick(ActionType.EXCEL) }
            holder.btnPrint.setOnClickListener { onActionClick(ActionType.PDF) }
            holder.btnDelete.setOnClickListener { onActionClick(ActionType.DELETE) }
        } else if (holder is ItemViewHolder) {
            val report = reports[position - 1]
            holder.tvName.text = report.studentName
            holder.tvRegNo.text = report.regNo
            holder.tvBranch.text = report.branch
            holder.tvRoom.text = report.roomNumber
            holder.tvBuilding.text = report.building
            holder.tvDate.text = report.date
            holder.tvTime.text = report.time
            holder.tvFaculty.text = report.invigilator
        }
    }

    override fun getItemCount() = reports.size + 1

    fun updateData(newReports: List<RoomWiseReportResponse>) {
        this.reports = newReports
        notifyDataSetChanged()
    }
}
