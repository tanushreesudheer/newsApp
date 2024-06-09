package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentFavouritesBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment() {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentFavouritesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        initRecyclerView()
        observeFavouriteArticles()

        newsAdapter.setOnItemClickListener { article ->
            // Handle favourite article click here
            openWebView(article)
        }

        // Setup swipe-to-delete
        setupSwipeToDelete()
    }

    private fun initRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerFavourites.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeFavouriteArticles() {
        viewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { favouriteArticles ->
            newsAdapter.differ.submitList(favouriteArticles.toList())
        })
    }

    private fun openWebView(article: Article) {
        val action = FavouritesFragmentDirections.actionFavouritesFragmentToArticleFragment(article)
        findNavController().navigate(action)
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not needed for swipe-to-delete
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Swipe-to-delete logic
                val position = viewHolder.adapterPosition
                val articleToDelete = newsAdapter.differ.currentList[position]

                // Delete the article
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.deleteArticle(articleToDelete)
                }

                // Show a snackbar with undo option
                Snackbar.make(binding.root, "Article removed from favorites", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // If "Undo" is clicked, reinsert the deleted article
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.addToFavourites(articleToDelete)
                        }
                    }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerFavourites)
        }
    }
}