package com.fanalbin.soedcare.ui.artikel

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.databinding.ItemArticleBinding
import com.fanalbin.soedcare.model.Article

class ArticleAdapter(private val context: Context) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private var articles = listOf<Article>()

    fun setArticles(articles: List<Article>) {
        this.articles = articles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvArticleTitle.text = article.title
            binding.tvArticleUrl.text = article.url

            binding.root.setOnClickListener {
                openArticleUrl(article.url)
            }
        }

        private fun openArticleUrl(url: String) {
            try {
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()

                customTabsIntent.launchUrl(context, Uri.parse(url))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}