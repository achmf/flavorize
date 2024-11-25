package com.example.flavorize.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavorize.databinding.FragmentHomeBinding
import com.example.flavorize.ui.fragments.home.viewmodel.HomeFragmentViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeFragmentViewModel by viewModels()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }
    }

    private fun observeViewModel() {
        homeViewModel.content.observe(viewLifecycleOwner) { content ->
            homeAdapter.updateContent(content)
        }
    }

    fun performSearch(query: String?) {
        query?.let {
            homeViewModel.content.value?.let { content ->
                val filteredContent = content.filter { item ->
                    item.title.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true)
                }
                homeAdapter.updateContent(filteredContent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
