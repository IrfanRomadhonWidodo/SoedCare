// QuestionDetailFragment.kt
package com.fanalbin.soedcare.ui.ask_soedcare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fanalbin.soedcare.databinding.FragmentQuestionDetailBinding
import com.fanalbin.soedcare.model.Answer
import com.fanalbin.soedcare.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class QuestionDetailFragment : Fragment() {

    private var _binding: FragmentQuestionDetailBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var questionId: String? = null
    private var question: Question? = null

    private lateinit var answerAdapter: AnswerAdapter
    private val answerList = mutableListOf<Answer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getQuestionId()
        setupRecyclerView()
        loadQuestion()
        setupClickListeners()
    }

    private fun getQuestionId() {
        questionId = arguments?.getString("QUESTION_ID")
        if (questionId == null) {
            Toast.makeText(requireContext(), "Pertanyaan tidak ditemukan", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        answerAdapter = AnswerAdapter(answerList)
        binding.rvAnswers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnswers.adapter = answerAdapter
    }

    private fun loadQuestion() {
        questionId?.let { id ->
            firestore.collection("questions").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        question = document.toObject(Question::class.java)
                        question?.id = document.id

                        // Get user data for the question
                        question?.userId?.let { userId ->
                            if (userId.isNotEmpty()) {
                                firestore.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { userDocument ->
                                        val userName = if (userDocument.exists()) {
                                            userDocument.getString("fullname") ?:
                                            userDocument.getString("fullName") ?:
                                            userDocument.getString("name") ?:
                                            "Unknown User"
                                        } else {
                                            "Unknown User"
                                        }

                                        Log.d("QuestionDetail", "User data for $userId: $userName")

                                        // Update question with user name
                                        question = question?.copy(userName = userName)

                                        // Display the question with user name
                                        displayQuestion()
                                        loadAnswers()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("QuestionDetail", "Error getting user data for $userId", e)

                                        // Use default name and proceed
                                        question = question?.copy(userName = "Unknown User")
                                        displayQuestion()
                                        loadAnswers()
                                    }
                            } else {
                                // Use default name and proceed
                                question = question?.copy(userName = "Unknown User")
                                displayQuestion()
                                loadAnswers()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Pertanyaan tidak ditemukan", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal memuat pertanyaan: ${e.message}", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
        }
    }

    private fun observeQuestionReplies() {
        questionId?.let { id ->
            firestore.collection("questions").document(id)
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    val replyCount = snapshot.getLong("replyCount") ?: 0
                    binding.tvReplyCount.text = "$replyCount Jawaban"
                }
        }
    }

    private fun loadAnswers() {
        questionId?.let { id ->
            firestore.collection("replies")
                .whereEqualTo("questionId", id)
                .get()
                .addOnSuccessListener { documents ->
                    answerList.clear()
                    for (document in documents) {
                        val answer = document.toObject(Answer::class.java)
                        answer.id = document.id
                        answerList.add(answer)
                    }

                    // Update reply count berdasarkan jumlah jawaban yang ada
                    binding.tvReplyCount.text = "${answerList.size} Jawaban"

                    if (answerList.isEmpty()) {
                        binding.tvNoAnswers.visibility = View.VISIBLE
                        binding.rvAnswers.visibility = View.GONE
                    } else {
                        binding.tvNoAnswers.visibility = View.GONE
                        binding.rvAnswers.visibility = View.VISIBLE
                        answerAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal memuat jawaban: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayQuestion() {
        question?.let { q ->
            binding.tvUserName.text = q.userName
            binding.tvQuestionTitle.text = q.title
            binding.tvQuestionContent.text = q.content
            binding.tvReplyCount.text = "${q.replyCount} Jawaban"

            // Format timestamp
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.tvQuestionDate.text = dateFormat.format(q.timestamp)

            // Show verified badge if answered by doctor
            binding.tvVerifiedBadge.visibility = if (q.answeredByDoctor) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendAnswer()
        }
    }

    private fun sendAnswer() {
        val answerContent = binding.etAnswer.text.toString().trim()
        if (answerContent.isEmpty()) {
            Toast.makeText(requireContext(), "Jawaban tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Anda harus login untuk menjawab pertanyaan", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user info
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { userDoc ->
                val userName = if (userDoc.exists()) {
                    userDoc.getString("fullname") ?:
                    userDoc.getString("fullName") ?:
                    userDoc.getString("name") ?:
                    "Anonymous"
                } else {
                    "Anonymous"
                }

                val isDoctor = userDoc.getBoolean("isDoctor") ?: false

                // Create answer
                val answer = hashMapOf(
                    "questionId" to question!!.id,
                    "userId" to currentUserId,
                    "userName" to userName,
                    "content" to answerContent,
                    "timestamp" to Date(),
                    "isDoctor" to isDoctor
                )

                // Add answer to Firestore
                firestore.collection("replies")
                    .add(answer)
                    .addOnSuccessListener { documentRef ->
                        // Update question reply count
                        firestore.collection("questions").document(question!!.id)
                            .update("replyCount", com.google.firebase.firestore.FieldValue.increment(1))

                        // If answered by doctor, update the flag
                        if (isDoctor) {
                            firestore.collection("questions").document(question!!.id)
                                .update("answeredByDoctor", true)
                        }

                        // 🔥 PERBAIKAN: Buat notifikasi untuk pemilik pertanyaan
                        createNotificationForQuestionOwner(question!!.id, answerContent, userName, isDoctor)

                        // Clear input field
                        binding.etAnswer.text.clear()

                        // Reload answers to show the new one
                        loadAnswers()

                        Toast.makeText(requireContext(), "Jawaban berhasil dikirim", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Gagal mengirim jawaban: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("QuestionDetail", "Error getting user data for $currentUserId", e)
                Toast.makeText(requireContext(), "Gagal memuat data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 PERBAIKAN: Method untuk membuat notifikasi
    private fun createNotificationForQuestionOwner(
        questionId: String,
        answerContent: String,
        answeredBy: String,
        isDoctor: Boolean
    ) {
        Log.d("Notification", "Creating notification for question: $questionId")

        // Ambil data pertanyaan untuk dapatkan info penanya
        firestore.collection("questions").document(questionId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val questionOwnerId = document.getString("userId")
                    val questionTitle = document.getString("title") ?: ""
                    val currentUserId = auth.currentUser?.uid

                    Log.d("Notification", "Question owner: $questionOwnerId, current user: $currentUserId")

                    // Hanya buat notifikasi jika penjawab bukan pemilik pertanyaan
                    if (questionOwnerId != null && questionOwnerId != currentUserId) {

                        // 🔥 generate doc id dulu
                        val notificationRef = firestore.collection("notifications").document()
                        val notificationId = notificationRef.id

                        val notificationData = hashMapOf(
                            "id" to notificationId,
                            "userId" to questionOwnerId, // ID pemilik pertanyaan
                            "questionId" to questionId,
                            "questionTitle" to questionTitle,
                            "answerContent" to answerContent,
                            "answeredBy" to answeredBy,
                            "isDoctor" to isDoctor,
                            "timestamp" to Date(),
                            "isRead" to false
                        )

                        // Simpan notifikasi langsung dengan ID
                        notificationRef.set(notificationData)
                            .addOnSuccessListener {
                                Log.d("Notification", "✅ Notifikasi berhasil dibuat dengan ID: $notificationId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Notification", "❌ Gagal membuat notifikasi", e)
                            }
                    } else {
                        Log.d("Notification", "Tidak membuat notifikasi karena penjawab adalah pemilik pertanyaan atau owner null")
                    }
                } else {
                    Log.e("Notification", "❌ Dokumen pertanyaan tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "❌ Gagal mengambil data pertanyaan", e)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}