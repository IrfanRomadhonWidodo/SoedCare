package com.fanalbin.soedcare.ui.artikel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fanalbin.soedcare.databinding.FragmentArtikelBinding
import com.fanalbin.soedcare.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ArtikelFragment : Fragment() {
    private var _binding: FragmentArtikelBinding? = null
    private val binding get() = _binding!!
    private lateinit var articleAdapter: ArticleAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtikelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadArticles()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter(requireContext())
        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
    }

    private fun loadArticles() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        Log.d("ArtikelFragment", "Memulai pengambilan data artikel dari Firestore...")

        firestore.collection("articles")
            .orderBy("title", Query.Direction.ASCENDING) // Opsional: mengurutkan berdasarkan judul
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                Log.d("ArtikelFragment", "Data diterima: ${result.size()} item")

                val articles = mutableListOf<Article>()

                if (result.isEmpty) {
                    Log.d("ArtikelFragment", "Tidak ada data artikel")
                    binding.tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                for (document in result) {
                    Log.d("ArtikelFragment", "Memproses artikel: ${document.id}")

                    try {
                        val title = document.getString("title")
                        val url = document.getString("url")
                        val id = document.id

                        Log.d("ArtikelFragment", "Artikel: id=$id, title=$title, url=$url")

                        if (title != null && url != null) {
                            val article = Article(id = id, title = title, url = url)
                            articles.add(article)
                        }
                    } catch (e: Exception) {
                        Log.e("ArtikelFragment", "Error memproses artikel: ${document.id}", e)
                    }
                }

                Log.d("ArtikelFragment", "Total artikel yang valid: ${articles.size}")

                if (articles.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    articleAdapter.setArticles(articles)
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                Log.e("ArtikelFragment", "Error mengambil data: ${exception.message}", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}