package com.example.newsapp.models

data class NewsResponses(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)