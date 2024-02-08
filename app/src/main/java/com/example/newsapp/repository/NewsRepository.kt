package com.example.newsapp.repository

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponses
import retrofit2.Response

class NewsRepository(val db:ArticleDatabase) {
    suspend fun getHeadlines(countryCode: String,pageNo:Int): Response<NewsResponses> =RetrofitInstance.api.getHeadlines(countryCode, pageNo)
    suspend fun searchnews(searchQuery: String,pageNo: Int):Response<NewsResponses> = RetrofitInstance.api.searchForNews(searchQuery, pageNo)
    suspend fun insertArticle(article: Article)=db.getArticleDao().insertArticle(article)
    fun getFavouriteNews()=db.getArticleDao().getAllArticles()
    suspend fun deleteArticles(article: Article)=db.getArticleDao().deleteArticle(article)
}