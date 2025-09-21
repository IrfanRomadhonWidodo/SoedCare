package com.fanalbin.soedcare.ui.ask_soedcare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.databinding.FragmentCreateQuestionBinding
import com.fanalbin.soedcare.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateQuestionFragment : Fragment() {

    private var _binding: FragmentCreateQuestionBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSubmitQuestion.setOnClickListener {
            submitQuestion()
        }
    }

    private fun submitQuestion() {
        val title = binding.etQuestionTitle.text.toString().trim()
        val content = binding.etQuestionContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etQuestionTitle.error = "Judul tidak boleh kosong"
            return
        }

        if (content.isEmpty()) {
            binding.etQuestionContent.error = "Isi pertanyaan tidak boleh kosong"
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Anda harus login untuk membuat pertanyaan", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitQuestion.isEnabled = false

        // Ambil data pengguna dari Firestore
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                // Ambil nama pengguna dari dokumen
                val userName = if (document.exists()) {
                    document.getString("fullname") ?:
                    document.getString("fullName") ?:
                    document.getString("name") ?:
                    currentUser.displayName ?: "Anonymous"
                } else {
                    currentUser.displayName ?: "Anonymous"
                }

                Log.d("CreateQuestion", "User name: $userName")

                // Buat objek pertanyaan dengan nama pengguna yang sudah diambil
                val question = Question(
                    userId = currentUser.uid,
                    userName = userName,
                    title = title,
                    content = content,
                    timestamp = Date(),
                    likes = 0,
                    likedBy = listOf(),
                    replyCount = 0,
                    answeredByDoctor = false
                )

                // Simpan pertanyaan ke Firestore
                firestore.collection("questions")
                    .add(question)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CreateQuestion", "Question created with ID: ${documentReference.id}")
                        Toast.makeText(requireContext(), "Pertanyaan berhasil dikirim", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CreateQuestion", "Error creating question", e)
                        Toast.makeText(requireContext(), "Gagal mengirim pertanyaan: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitQuestion.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CreateQuestion", "Error getting user data", e)
                // Jika gagal mengambil data pengguna, gunakan displayName sebagai fallback
                val question = Question(
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Anonymous",
                    title = title,
                    content = content,
                    timestamp = Date(),
                    likes = 0,
                    likedBy = listOf(),
                    replyCount = 0,
                    answeredByDoctor = false
                )

                // Simpan pertanyaan ke Firestore
                firestore.collection("questions")
                    .add(question)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CreateQuestion", "Question created with ID: ${documentReference.id}")
                        Toast.makeText(requireContext(), "Pertanyaan berhasil dikirim", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener { e2 ->
                        Log.e("CreateQuestion", "Error creating question", e2)
                        Toast.makeText(requireContext(), "Gagal mengirim pertanyaan: ${e2.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmitQuestion.isEnabled = true
                    }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}