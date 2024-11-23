package com.example.flavorize.ui.fragments.myrecipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.databinding.FragmentMyRecipesBinding
import com.example.flavorize.ui.fragments.recipes.RecipesAdapter
import com.example.flavorize.ui.fragments.myrecipe.viewmodel.MyRecipesFragmentViewModel

class MyRecipesFragment : Fragment() {
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyRecipesFragmentViewModel by viewModels()
    private val recipesAdapter = RecipesAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSearchListener()
        viewModel.fetchMyRecipes()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.myRecipesRecyclerView.layoutManager = gridLayoutManager
        binding.myRecipesRecyclerView.adapter = recipesAdapter
    }

    private fun observeViewModel() {
        viewModel.myRecipes.observe(viewLifecycleOwner) { recipes ->
            recipesAdapter.updateRecipes(recipes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchListener() {
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchBar.text.toString()
                performSearch(query)
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        viewModel.myRecipes.value?.let { recipes ->
            val filteredRecipes = recipes.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
            recipesAdapter.updateRecipes(filteredRecipes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
