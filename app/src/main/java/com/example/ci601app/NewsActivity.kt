package com.example.ci601app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ci601app.adapters.RecyclerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import com.example.ci601app.databinding.ActivityNewsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.withContext

const val BASE_URL = "https://api.currentsapi.services" // Ensure no space in the URL

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private var titlesList = mutableListOf<String>()
    private var descList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var linksList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecyclerView()
        setupBottomNavigation()
        makeAPIRequest()
    }

    private fun setUpRecyclerView() {
        binding.rvRecylerView.layoutManager = LinearLayoutManager(this)
        binding.rvRecylerView.adapter = RecyclerAdapter(titlesList, descList, imageList, linksList)
    }


    private fun addToList(title: String, description: String, image: String, link: String) {
        titlesList.add(title)
        descList.add(description)
        imageList.add(image)
        linksList.add(link)
    }

    private fun makeAPIRequest() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIRequest::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getNewsByDomain(
                    apiKey = "kJTmY-qD8JFuBm3VDjYCOr7GrV_hzXtpHBSL0pKZXsetMJGe",
                    domain = "thehackernews.com"
                )
                if (response.isSuccessful) {
                    val newsArticles = response.body()?.news
                    if (!newsArticles.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            newsArticles.forEach { article ->
                                addToList(
                                    article.title,
                                    article.description,
                                    article.image,
                                    article.url
                                )
                            }
                            setUpRecyclerView()
                        }
                    } else {
                        Log.e("NewsActivity", "No articles found")
                    }
                } else {
                    Log.e("NewsActivity", "API Request failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsActivity", "Exception in making API request", e)
            }
        }
    }


    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
}