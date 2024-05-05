package com.example.ci601app.api

data class NewsApiJSON(
    val news: List<New>,
    val page: Int,
    val status: String
)