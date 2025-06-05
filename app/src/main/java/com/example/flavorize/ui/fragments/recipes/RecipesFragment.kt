package com.example.flavorize.ui.fragments.recipes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.databinding.FragmentRecipesBinding
import com.example.flavorize.ui.activities.createform.CreateRecipeFormActivity
import com.example.flavorize.ui.fragments.recipes.paging.RecipesPagingAdapter
import com.example.flavorize.ui.fragments.recipes.viewmodel.RecipesFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipesFragment : Fragment() {
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    // ViewModel for managing recipes data and bookmark state
    private val recipesViewModel: RecipesFragmentViewModel by viewModels()

    // Adapter for paginated recipe data
    private lateinit var recipesPagingAdapter: RecipesPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize loading overlay without custom text
        binding.loadingView.loadingOverlay.visibility = View.VISIBLE
        binding.loadingView.loadingText.text = "Loading"

        // Setup RecyclerView for displaying recipes
        setupRecyclerView()

        // Setup listeners for UI interactions
        setupListeners()

        // Observe ViewModel for errors and other updates
        observeViewModel()

        // Collect paginated recipes and submit them to the adapter
        lifecycleScope.launch {
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
                // Hide loading overlay after data is loaded
                binding.loadingView.loadingOverlay.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Start listening for bookmark updates
            recipesViewModel.startListeningForBookmarkChanges(userId)
            recipesViewModel.bookmarkUpdates.observe(viewLifecycleOwner) { updatedBookmarks ->
                lifecycleScope.launch {
                    recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                        // Update bookmark status locally
                        val updatedData = pagingData.map { recipe ->
                            recipe.copy(isBookmarked = updatedBookmarks.contains(recipe.id))
                        }
                        recipesPagingAdapter.submitData(updatedData)
                    }
                }
            }
        } else {
            // Show message if user is not logged in
            Toast.makeText(requireContext(), "Please log in to see bookmarks", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop listening for bookmark updates
        recipesViewModel.stopListeningForBookmarkChanges()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with bookmark toggle callback
        recipesPagingAdapter = RecipesPagingAdapter { recipe, isBookmarking, onComplete ->
            recipesViewModel.toggleBookmark(recipe, isBookmarking, {
                // Show success message
                onComplete(true)
                Toast.makeText(
                    requireContext(),
                    if (isBookmarking) "Bookmarked!" else "Bookmark removed!",
                    Toast.LENGTH_SHORT
                ).show()
            }, { error ->
                // Show error message on failure
                onComplete(false)
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            })
        }

        // Set layout manager and adapter for RecyclerView
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipesRecyclerView.layoutManager = gridLayoutManager
        binding.recipesRecyclerView.adapter = recipesPagingAdapter
    }

    private fun setupListeners() {
        // Handle the create recipe button click
        binding.createRecipeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreateRecipeFormActivity::class.java))
        }
    }

    private fun observeViewModel() {
        // Observe errors from the ViewModel
        recipesViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performSearch(query: String?) {
        query?.let {
            lifecycleScope.launch {
                // Clear current data before performing search
                recipesPagingAdapter.submitData(PagingData.empty())
                recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                    // Filter recipes based on search query
                    val filteredPagingData = pagingData.filter { recipe ->
                        recipe.name.contains(query, ignoreCase = true) ||
                                recipe.description.contains(query, ignoreCase = true)
                    }
                    recipesPagingAdapter.submitData(filteredPagingData)
                }
            }
        }
    }

    fun resetSearch() {
        lifecycleScope.launch {
            // Reset search and show all recipes
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding when the view is destroyed
        _binding = null
    }
}
