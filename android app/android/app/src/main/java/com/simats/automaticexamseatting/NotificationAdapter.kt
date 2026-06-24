package com.simats.automaticexamseatting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private var notifications: MutableList<Notification>,
    private val onDeleteClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
        val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvTime.text = notification.time

        if (!notification.isRead) {
            holder.tvBadge.visibility = View.VISIBLE
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        // Set icon based on title or content
        if (notification.title.contains("Conflict", ignoreCase = true)) {
            holder.ivStatus.setImageResource(R.drawable.ic_error)
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle)
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newList: List<Notification>) {
        notifications = newList.toMutableList()
        notifyDataSetChanged()
    }
}
