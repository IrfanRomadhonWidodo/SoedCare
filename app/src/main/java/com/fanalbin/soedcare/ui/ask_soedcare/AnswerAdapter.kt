package com.fanalbin.soedcare.ui.ask_soedcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Answer
import java.text.SimpleDateFormat
import java.util.*

class AnswerAdapter(private val answers: List<Answer>) :
    RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_answer, parent, false)
        return AnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = answers[position]
        holder.bind(answer)
    }

    override fun getItemCount() = answers.size

    inner class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_answer_user_name)
        private val tvDoctorBadge: TextView = itemView.findViewById(R.id.tv_doctor_badge)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_answer_content)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_answer_date)

        fun bind(answer: Answer) {
            tvUserName.text = answer.userName
            tvContent.text = answer.content

            // Format timestamp
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvDate.text = dateFormat.format(answer.timestamp)

            // Show doctor badge if answered by doctor
            tvDoctorBadge.visibility = if (answer.isDoctor) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}