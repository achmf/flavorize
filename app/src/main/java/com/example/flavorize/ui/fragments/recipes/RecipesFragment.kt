package com.example.flavorize.ui.fragments.recipes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.databinding.FragmentRecipesBinding
import com.example.flavorize.ui.activities.createform.CreateRecipeFormActivity
import com.example.flavorize.ui.fragments.recipes.viewmodel.RecipesFragmentViewModel
import com.google.firebase.auth.FirebaseAuth

class RecipesFragment : Fragment() {
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val recipesViewModel: RecipesFragmentViewModel by viewModels()
    private lateinit var recipesAdapter: RecipesAdapter // Delay initialization

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter after context and userId are available
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        recipesAdapter = RecipesAdapter(
            allRecipes = mutableListOf(),
            userId = userId,
            onBookmarkToggle = { recipe, isBookmarking ->
                recipesViewModel.toggleBookmark(recipe, isBookmarking, onSuccess = {
                    recipesAdapter.updateRecipe(recipe.copy(isBookmarked = isBookmarking))
                    Toast.makeText(
                        requireContext(),
                        if (isBookmarking) "Bookmarked!" else "Bookmark removed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }, onError = {
                    Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
                })
            }
        )

        setupRecyclerView()
        setupListeners()
        setupSwipeToRefresh()
        observeViewModel()

        recipesViewModel.fetchRecipesWithBookmarks()
    }

    override fun onResume() {
        super.onResume()
        recipesViewModel.fetchRecipesWithBookmarks() // Refresh data when fragment becomes visible again
    }


    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipesRecyclerView.layoutManager = gridLayoutManager
        binding.recipesRecyclerView.adapter = recipesAdapter
    }

    private fun setupListeners() {
        binding.createRecipeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreateRecipeFormActivity::class.java))
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            recipesViewModel.fetchRecipesWithBookmarks()
        }
    }

    private fun observeViewModel() {
        recipesViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipesAdapter.updateRecipes(recipes)
            binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
        }

        recipesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        recipesViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
            }
        }
    }

    fun performSearch(query: String?) {
        query?.let {
            recipesViewModel.recipes.value?.let { recipes ->
                val filteredRecipes = recipes.filter { recipe ->
                    recipe.name.contains(query, ignoreCase = true) || recipe.description.contains(query, ignoreCase = true)
                }
                recipesAdapter.updateRecipes(filteredRecipes)
            }
        }
    }

    fun resetSearch() {
        // Replace this logic with what resets the displayed recipes to the original full list
        recipesViewModel.recipes.value?.let { recipesAdapter.updateRecipes(it) }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
