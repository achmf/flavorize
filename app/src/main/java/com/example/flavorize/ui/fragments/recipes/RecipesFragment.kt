package com.example.flavorize.ui.fragments.recipes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.databinding.FragmentRecipesBinding
import com.example.flavorize.ui.createform.CreateRecipeFormActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecipesFragment : Fragment() {
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!
    private val firestoreRepository = FirestoreRepository()
    private val recipesAdapter = RecipesAdapter(listOf())
    private var fetchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        fetchRecipes()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // Set 2 columns
        binding.recipesRecyclerView.layoutManager = gridLayoutManager
        binding.recipesRecyclerView.adapter = recipesAdapter
    }

    private fun setupListeners() {
        binding.searchBar.addTextChangedListener { text ->
            filterRecipes(text.toString())
        }

        binding.createRecipeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreateRecipeFormActivity::class.java))
        }
    }

    private fun fetchRecipes() {
        fetchJob?.cancel()
        fetchJob = lifecycleScope.launch {
            val result = firestoreRepository.getAllRecipes()
            if (result.isSuccess) {
                recipesAdapter.updateRecipes(result.getOrDefault(emptyList()))
            }
        }
    }

    private fun filterRecipes(query: String) {
        val filteredList = recipesAdapter.allRecipes.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        recipesAdapter.updateRecipes(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fetchJob?.cancel()
    }
}