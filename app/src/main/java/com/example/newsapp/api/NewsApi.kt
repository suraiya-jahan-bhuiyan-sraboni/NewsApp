package com.example.newsapp.api

import com.example.newsapp.models.NewsResponses
import com.example.newsapp.util.Constants.Companion.api_key
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("county")
        countryCode: String="us",
        @Query("page")
        page: Int=1 ,
        @Query("apiKey")
    apikey:String = api_key
    ):Response<NewsResponses>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        page: Int=1,
        @Query("apiKey")
        apikey: String= api_key
    ):Response<NewsResponses>

}