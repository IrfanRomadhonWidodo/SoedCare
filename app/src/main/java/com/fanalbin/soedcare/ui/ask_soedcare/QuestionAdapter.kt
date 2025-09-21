package com.fanalbin.soedcare.ui.ask_soedcare

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.databinding.ItemQuestionBinding
import com.fanalbin.soedcare.model.Question
import java.text.SimpleDateFormat
import java.util.*

class QuestionAdapter(
    private var questions: List<Question>,
    private val currentUserId: String,
    private val onLikeClicked: (Question) -> Unit,
    private val onQuestionClicked: (Question) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount() = questions.size

    fun updateData(newQuestions: List<Question>) {
        questions = newQuestions
        notifyDataSetChanged()
    }

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {
            binding.tvUserName.text = question.userName
            binding.tvQuestionTitle.text = question.title
            binding.tvQuestionContent.text = question.content
            binding.tvReplyCount.text = "${question.replyCount}"
            binding.tvLikeCount.text = question.likes.toString()

            // Format timestamp to readable date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvQuestionDate.text = dateFormat.format(question.timestamp)

            // Show verified badge if answered by doctor
            binding.tvVerifiedBadge.visibility = if (question.answeredByDoctor) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // Check if current user has liked this question
            val isLiked = question.likedBy.contains(currentUserId)

            // Set like icon based on like status
            if (isLiked) {
                binding.ivLike.setImageResource(R.drawable.ic_like) // Gunakan ikon yang sama
                binding.ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
            } else {
                binding.ivLike.setImageResource(R.drawable.ic_like) // Gunakan ikon yang sama
                binding.ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.gray))
            }

            // Handle like button click
            binding.ivLike.setOnClickListener {
                onLikeClicked(question)
            }

            // Handle question item click
            itemView.setOnClickListener {
                onQuestionClicked(question)
            }
        }
    }
}