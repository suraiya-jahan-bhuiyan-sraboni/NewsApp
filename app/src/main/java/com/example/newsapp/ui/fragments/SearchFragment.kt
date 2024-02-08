package com.example.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.search_news_time_delay
import com.example.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentSearchBinding.bind(view)
        itemSearchError=view.findViewById(R.id.itemSearchError)
        val inflater=requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view:View=inflater.inflate(R.layout.item_error,null)
        retryButton=view.findViewById(R.id.retryButton)
        errorText=view.findViewById(R.id.errorText)
        newsViewModel=(activity as NewsActivity).newsViewModel
        setupSearchRecycler()
        newsAdapter.setOnItemCLickListener {
            val bundle=Bundle().apply {
                putSerializable("Result",it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment,bundle)
        }
        var job: Job?=null
        binding.searchEdit.addTextChangedListener(){editable->
            job?.cancel()
            job= MainScope().launch {
                delay(search_news_time_delay)
                editable?.let {
                    if (editable.toString().isNotEmpty()){
                        newsViewModel.searchnews(editable.toString())
                    }
                }
            }
        }
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/Constants.query_page_size+2
                        isLastPage=newsViewModel.searchNewsPage==totalPages
                        if (isLastPage){
                            binding.recyclerSearch.setPadding(0,0,0,0)
                        }

                    }
                }
                is Resource.error<*> -> {
                    hideProgressBar()
                    response.messgae?.let {message->
                        Toast.makeText(activity, "sorry error: $message", Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.loading<*> -> {
                    showProgessBar()
                }
            }
        })
        retryButton.setOnClickListener(){
            if (binding.searchEdit.text.toString().isNotEmpty()){
                newsViewModel.searchnews(binding.searchEdit.text.toString())
            }else{
                hideErrorMessage()
            }
        }
    }

    var isError=false
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility=View.INVISIBLE
        isLoading=false
    }
    private fun showProgessBar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=true
    }
    private fun hideErrorMessage(){
        itemSearchError.visibility=View.INVISIBLE
        isError=false
    }
    private fun showErrorMessage(msg:String){
        itemSearchError.visibility=View.VISIBLE
        errorText.text=msg
        isError=true
    }
    val scrollListener=object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount

            val isNoError=!isError
            val isNotLoadingAndNotLastPage=!isLoading&&!isLastPage
            val isAtLastItem=firstVisibleItemPosition+visibleItemCount >= totalItemCount
            val isNotAtBeginning=firstVisibleItemPosition>=0
            val isTotalMoreThanVisible=totalItemCount>= Constants.query_page_size
            val shouldPaginate = isNoError&&isNotLoadingAndNotLastPage&&isAtLastItem&&isNotAtBeginning&&isTotalMoreThanVisible&&isScrolling
            if (shouldPaginate){
                newsViewModel.searchnews(binding.searchEdit.text.toString())
                isScrolling=false
            }
        }
    }
    private fun setupSearchRecycler(){
        newsAdapter= NewsAdapter()
        binding.recyclerSearch.apply {
            adapter=newsAdapter
            layoutManager=LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

}