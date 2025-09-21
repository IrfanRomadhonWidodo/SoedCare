package com.fanalbin.soedcare.ui.ask_soedcare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fanalbin.soedcare.ContainerActivity
import com.fanalbin.soedcare.databinding.FragmentAskSoedcareBinding
import com.fanalbin.soedcare.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.Toast
import java.util.*
import android.util.Log

class AskSoedcareFragment : Fragment() {
    private var _binding: FragmentAskSoedcareBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var questionAdapter: QuestionAdapter

    // Current user ID
    private val currentUserId = auth.currentUser?.uid ?: ""

    // List to hold all questions
    private val allQuestions = mutableListOf<Question>()
    private val myQuestions = mutableListOf<Question>()
    private val favoriteQuestions = mutableListOf<Question>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAskSoedcareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        setupClickListeners()
        loadQuestions()
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(
            questions = allQuestions,
            currentUserId = currentUserId,
            onLikeClicked = { question -> handleLikeQuestion(question) },
            onQuestionClicked = { question -> navigateToQuestionDetail(question) }
        )

        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> questionAdapter.updateData(allQuestions)
                    1 -> questionAdapter.updateData(myQuestions)
                    2 -> questionAdapter.updateData(favoriteQuestions)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                // Do nothing
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnAskQuestion.setOnClickListener {
            navigateToCreateQuestion()
        }

        binding.fabAdd.setOnClickListener {
            navigateToCreateQuestion()
        }
    }

    private fun loadQuestions() {
        firestore.collection("questions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("AskSoedcareFragment", "Error loading questions", exception)
                    return@addSnapshotListener
                }

                allQuestions.clear()
                myQuestions.clear()
                favoriteQuestions.clear()

                val questionsList = mutableListOf<Question>()

                snapshot?.documents?.forEach { document ->
                    val question = document.toObject(Question::class.java)
                    question?.id = document.id

                    if (question != null) {
                        questionsList.add(question)
                    }
                }

                // Process each question to get user data
                val processedQuestions = mutableListOf<Question>()
                var processedCount = 0

                if (questionsList.isEmpty()) {
                    updateAdapterWithCurrentTab()
                    return@addSnapshotListener
                }

                questionsList.forEach { question ->
                    // Skip if userId is empty
                    if (question.userId.isEmpty()) {
                        val questionWithDefaultName = question.copy(userName = "Unknown User")
                        processedQuestions.add(questionWithDefaultName)
                        processedCount++

                        if (processedCount == questionsList.size) {
                            finishProcessingQuestions(processedQuestions)
                        }
                        return@forEach
                    }

                    // Get user data from Firestore
                    firestore.collection("users").document(question.userId)
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

                            Log.d("AskSoedcareFragment", "User data for ${question.userId}: $userName")

                            // Create a new question with the user name
                            val questionWithUserName = question.copy(userName = userName)
                            processedQuestions.add(questionWithUserName)

                            processedCount++
                            if (processedCount == questionsList.size) {
                                finishProcessingQuestions(processedQuestions)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AskSoedcareFragment", "Error getting user data for ${question.userId}", e)

                            // Create a new question with default name
                            val questionWithDefaultName = question.copy(userName = "Unknown User")
                            processedQuestions.add(questionWithDefaultName)

                            processedCount++
                            if (processedCount == questionsList.size) {
                                finishProcessingQuestions(processedQuestions)
                            }
                        }
                }
            }
    }

    private fun finishProcessingQuestions(questions: List<Question>) {
        allQuestions.clear()
        myQuestions.clear()
        favoriteQuestions.clear()

        questions.forEach { question ->
            allQuestions.add(question)

            if (question.userId == currentUserId) {
                myQuestions.add(question)
            }

            if (question.likedBy.contains(currentUserId)) {
                favoriteQuestions.add(question)
            }

            // ✅ Tambahkan observer realtime replyCount
            observeReplyCount(question.id)
        }

        updateAdapterWithCurrentTab()
    }

    private fun observeReplyCount(questionId: String) {
        firestore.collection("replies")
            .whereEqualTo("questionId", questionId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val replyCount = snapshot.size()
                val updatedQuestions = allQuestions.map { q ->
                    if (q.id == questionId) q.copy(replyCount = replyCount) else q
                }

                allQuestions.clear()
                allQuestions.addAll(updatedQuestions)

                myQuestions.clear()
                myQuestions.addAll(allQuestions.filter { it.userId == currentUserId })

                favoriteQuestions.clear()
                favoriteQuestions.addAll(allQuestions.filter { it.likedBy.contains(currentUserId) })

                updateAdapterWithCurrentTab()
            }
    }

    private fun updateAdapterWithCurrentTab() {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> questionAdapter.updateData(allQuestions)
            1 -> questionAdapter.updateData(myQuestions)
            2 -> questionAdapter.updateData(favoriteQuestions)
        }
    }
    private fun navigateToCreateQuestion() {
        val intent = Intent(requireContext(), ContainerActivity::class.java).apply {
            putExtra(ContainerActivity.FRAGMENT_TYPE, ContainerActivity.FRAGMENT_CREATE_QUESTION)
        }
        startActivity(intent)
    }

    private fun handleLikeQuestion(question: Question) {
        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val questionRef = firestore.collection("questions").document(question.id)
        val hasLiked = question.likedBy.contains(currentUserId)

        // Gunakan transaction untuk memastikan konsistensi data
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(questionRef)
            val currentQuestion = snapshot.toObject(Question::class.java) ?: throw Exception("Question not found")

            val currentLikes = currentQuestion.likes
            val currentLikedBy = currentQuestion.likedBy.toMutableList()

            if (hasLiked) {
                // Unlike the question
                if (currentLikedBy.contains(currentUserId)) {
                    currentLikedBy.remove(currentUserId)
                    transaction.update(questionRef, "likes", currentLikes - 1)
                    transaction.update(questionRef, "likedBy", currentLikedBy)
                }
            } else {
                // Like the question
                if (!currentLikedBy.contains(currentUserId)) {
                    currentLikedBy.add(currentUserId)
                    transaction.update(questionRef, "likes", currentLikes + 1)
                    transaction.update(questionRef, "likedBy", currentLikedBy)
                }
            }
        }.addOnSuccessListener {
            Log.d("AskSoedcareFragment", "Transaction success")

            // Update local data immediately
            val updatedQuestions = allQuestions.map { q ->
                if (q.id == question.id) {
                    if (hasLiked) {
                        q.copy(likes = q.likes - 1, likedBy = q.likedBy - currentUserId)
                    } else {
                        q.copy(likes = q.likes + 1, likedBy = q.likedBy + currentUserId)
                    }
                } else {
                    q
                }
            }

            // Update the lists
            allQuestions.clear()
            allQuestions.addAll(updatedQuestions)

            // Update myQuestions if needed
            myQuestions.clear()
            myQuestions.addAll(allQuestions.filter { it.userId == currentUserId })

            // Update favoriteQuestions
            favoriteQuestions.clear()
            favoriteQuestions.addAll(allQuestions.filter { it.likedBy.contains(currentUserId) })

            // Refresh the adapter
            updateAdapterWithCurrentTab()
        }.addOnFailureListener { e ->
            Log.e("AskSoedcareFragment", "Transaction failure", e)
            Toast.makeText(requireContext(), "Gagal memperbarui like: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToQuestionDetail(question: Question) {
        val intent = Intent(requireContext(), ContainerActivity::class.java).apply {
            putExtra(ContainerActivity.FRAGMENT_TYPE, ContainerActivity.FRAGMENT_QUESTION_DETAIL)
            putExtra(ContainerActivity.QUESTION_ID, question.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}