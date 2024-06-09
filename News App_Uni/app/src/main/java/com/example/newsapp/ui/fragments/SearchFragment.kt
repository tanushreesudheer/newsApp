package com.example.newsapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.ui.fragments.SearchFragmentDirections
import com.example.newsapp.util.Resource

class SearchFragment : Fragment() {

    private lateinit var viewModel: NewsViewModel
    private lateinit var searchAdapter: NewsAdapter
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        initRecyclerView()
        observeSearchResults()

        // Perform search when the user clicks the search button
        binding.searchEdit.setOnClickListener {
            val query = binding.searchEdit.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchNews(query)
            }
        }

        binding.searchEdit.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = binding.searchEdit.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchNews(query)
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // Handle article click here
        searchAdapter.setOnItemClickListener { article ->
            val action = SearchFragmentDirections.actionSearchFragmentToArticleFragment(article)
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerView() {
        searchAdapter = NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext()) // Use requireContext() here
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        hideKeyboard()
                    }
                }
            })
        }
    }

    @SuppressLint("ServiceCast")
    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
    }


    private fun observeSearchResults() {
        viewModel.searchNews.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.paginationProgressBar.visibility = View.GONE
                    resource.data?.let { newsResponse ->
                        val articles = newsResponse.articles
                        searchAdapter.differ.submitList(articles)
                    }
                }
                is Resource.Error -> {
                    binding.paginationProgressBar.visibility = View.GONE
                    // Handle error state and show an error message if needed
                }
                is Resource.Loading -> {
                    binding.paginationProgressBar.visibility = View.VISIBLE
                }
            }
        })
    }
}