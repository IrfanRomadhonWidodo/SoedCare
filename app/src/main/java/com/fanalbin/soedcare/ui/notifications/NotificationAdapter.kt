package com.fanalbin.soedcare.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Notification
import java.util.*

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notifikasi, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotificationTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        private val tvNotificationTime: TextView = itemView.findViewById(R.id.tv_notification_time)
        private val viewUnreadIndicator: View = itemView.findViewById(R.id.view_unread_indicator)
        private val tvQuestionTitle: TextView = itemView.findViewById(R.id.tv_question_title)
        private val tvAnswerPreview: TextView = itemView.findViewById(R.id.tv_answer_preview)
        private val tvAnsweredBy: TextView = itemView.findViewById(R.id.tv_answered_by)
        private val tvDoctorBadge: TextView = itemView.findViewById(R.id.tv_doctor_badge)

        fun bind(notification: Notification) {
            tvNotificationTitle.text = "Balasan pada pertanyaan Anda"
            tvQuestionTitle.text = "Pertanyaan: ${notification.questionTitle}"
            tvAnswerPreview.text = notification.answerContent
            tvAnsweredBy.text = "Dijawab oleh: ${notification.answeredBy}"

            // Format waktu
            val diff = Date().time - notification.timestamp.time
            val minutes = diff / 60000
            val hours = minutes / 60
            val days = hours / 24

            tvNotificationTime.text = when {
                days > 0 -> "$days hari yang lalu"
                hours > 0 -> "$hours jam yang lalu"
                minutes > 0 -> "$minutes menit yang lalu"
                else -> "Baru saja"
            }

            // Indikator unread
            viewUnreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Badge dokter
            tvDoctorBadge.visibility = if (notification.isDoctor) View.VISIBLE else View.GONE

            // Klik
            itemView.setOnClickListener { onItemClick(notification) }
        }
    }
}
