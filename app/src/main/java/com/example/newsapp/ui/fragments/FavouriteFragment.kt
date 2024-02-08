package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentFavouriteBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class FavouriteFragment : Fragment(R.layout.fragment_favourite) {

lateinit var newsViewModel: NewsViewModel
lateinit var newsAdapter: NewsAdapter
lateinit var binding: FragmentFavouriteBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentFavouriteBinding.bind(view)
        newsViewModel=(activity as NewsActivity).newsViewModel
        setUpFavouriteRecyclerView()
        newsAdapter.setOnItemCLickListener {
            val bundle=Bundle().apply {
                putSerializable("Result",it)
            }
            findNavController().navigate(R.id.action_favouritesFragment_to_articleFragment,bundle)
        }
        val itemTouchHelperCallback=
            object :ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position=viewHolder.adapterPosition
                    val article= newsAdapter.differ.currentList[position]
                    newsViewModel.deleteArticle(article)
                    Snackbar.make(view,"Removed from Favourites!",Snackbar.LENGTH_LONG).apply {
                        setAction("Undo"){
                            newsViewModel.addToFavourites(article)
                        }
                        show()
                    }
                }
            }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerFavourites)
        }
        newsViewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { article->
            newsAdapter.differ.submitList(article)
        })
    }
private fun setUpFavouriteRecyclerView(){
    newsAdapter= NewsAdapter()
    binding.recyclerFavourites.apply {
        adapter=newsAdapter
        layoutManager= LinearLayoutManager(activity)
    }
}
}