package com.example.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponses
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application, private val newsRepository: NewsRepository): AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponses>> = MutableLiveData()
    var headlinesPage=1
    private var headlinesResponse: NewsResponses?=null
    val searchNews: MutableLiveData<Resource<NewsResponses>> = MutableLiveData()
    var searchNewsPage=1
    private var searchNewsResponse: NewsResponses?=null
    private var newSearchQuery:String?=null
    private var oldSearchQuery:String?=null
    init {
        getHeadlines("us")
    }
    fun getHeadlines(countryCode: String)= CoroutineScope(Dispatchers.IO).launch {
        headlinesInternet(countryCode)
    }
    fun searchnews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }
    private fun handleHeadlinesResponse(response: Response<NewsResponses>):Resource<NewsResponses>{
        if (response.isSuccessful){
            response.body()?.let {resultResponse->
                headlinesPage++
                if(headlinesResponse==null){
                    headlinesResponse=resultResponse
                }else{
                    val oldArticles=headlinesResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse?:resultResponse)
            }
        }
        return Resource.error(response.message())
    }
    private fun handleSearchNewsResponse(response: Response<NewsResponses>):Resource<NewsResponses>{
        if (response.isSuccessful){
            response.body()?.let {resultResponse->

                if(searchNewsResponse==null||newSearchQuery!=oldSearchQuery){
                    searchNewsPage=1
                    oldSearchQuery=newSearchQuery
                    searchNewsResponse=resultResponse
                }else{
                    searchNewsPage++
                    val oldArticles=headlinesResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.error(response.message())
    }
    fun addToFavourites(article: Article)=viewModelScope.launch{
        newsRepository.insertArticle(article)
    }
    fun getFavouriteNews()=newsRepository.getFavouriteNews()
    fun deleteArticle(article: Article)=viewModelScope.launch{
        newsRepository.deleteArticles(article)
    }
    private fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)-> true
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> true
                    else->false
                }
            }?:false
        }
    }
    private suspend fun headlinesInternet(countryCode:String){
        headlines.postValue(Resource.loading())
        try {
            if (internetConnection(this.getApplication())){
                val response=newsRepository.getHeadlines(countryCode,headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            }else{
                headlines.postValue(Resource.error("No Internet Connections"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException ->headlines.postValue(Resource.error("Unable To Connect"))
                else-> {
                    headlines.postValue(Resource.error("No Signal"))

                    Log.e("NewsViewModel", "Exception: ${t.message}",t)
                }
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery:String){
        newSearchQuery=searchQuery
        searchNews.postValue(Resource.loading())
        try {
            if (internetConnection(this.getApplication())){

                val response=newsRepository.searchnews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))

            }else{
                searchNews.postValue(Resource.error("No Internet Connections"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException ->searchNews.postValue(Resource.error("Unable To Connect"))
                else->searchNews.postValue(Resource.error("No Signal"))
            }
        }
    }
}
