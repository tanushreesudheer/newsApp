package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentHeadlineBinding
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.ui.fragments.HeadlinesFragmentDirections
import com.example.newsapp.util.Resource

class HeadlinesFragment : Fragment() {

    private lateinit var viewModel: NewsViewModel
    private lateinit var headlinesAdapter: NewsAdapter
    private lateinit var binding: FragmentHeadlineBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHeadlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        initRecyclerView()
        observeHeadlines()

        headlinesAdapter.setOnItemClickListener { article ->
            // Handle article click here
            val action = HeadlinesFragmentDirections.actionHeadlinesFragmentToArticleFragment(article)
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerView() {
        headlinesAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = headlinesAdapter
            layoutManager = LinearLayoutManager(requireContext()) // Use requireContext() here
        }
    }

    private fun observeHeadlines() {
        viewModel.headlines.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.paginationProgressBar.visibility = View.GONE
                    resource.data?.let { newsResponse ->
                        val articles = newsResponse.articles
                        headlinesAdapter.differ.submitList(articles)
                    }
                }
                is Resource.Error -> {
                    binding.paginationProgressBar.visibility = View.GONE
                    binding.itemHeadlinesError.retryButton.setOnClickListener {
                        // Handle retry logic here
                        viewModel.getHeadlines("us")
                    }
                }
                is Resource.Loading -> {
                    binding.paginationProgressBar.visibility = View.VISIBLE
                }
            }
        })
    }
}
