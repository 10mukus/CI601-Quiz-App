package com.example.ci601app

import com.example.ci601app.api.NewsApiJSON
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface APIRequest {
    @GET("/v1/latest-news")
    suspend fun getNewsByDomain(
        @Query("kJTmY-qD8JFuBm3VDjYCOr7GrV_hzXtpHBSL0pKZXsetMJGe") apiKey: String,
        @Query("thehackernews.com") domain: String,
        @Query("language") language: String = "en"  // Optional: defaulting to English
    ): Response<NewsApiJSON>
}