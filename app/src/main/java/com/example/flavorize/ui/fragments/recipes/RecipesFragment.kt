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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.databinding.FragmentRecipesBinding
import com.example.flavorize.ui.activities.createform.CreateRecipeFormActivity
import com.example.flavorize.ui.fragments.recipes.paging.RecipesPagingAdapter
import com.example.flavorize.ui.fragments.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipesFragment : Fragment() {
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val recipesViewModel: RecipesFragmentViewModel by viewModels()
    private lateinit var recipesPagingAdapter: RecipesPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupSwipeToRefresh()
        observeViewModel()
        observePagingData()

        // Observe bookmark changes
        observeBookmarkChanges()

        lifecycleScope.launch {
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data Paging
        lifecycleScope.launch {
            recipesPagingAdapter.refresh() // Refresh dataset
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupRecyclerView() {
        recipesPagingAdapter = RecipesPagingAdapter { recipe, isBookmarking ->
            recipesViewModel.toggleBookmark(recipe, isBookmarking, {
                // Success: No need to refresh here
                Toast.makeText(
                    requireContext(),
                    if (isBookmarking) "Bookmarked!" else "Bookmark removed!",
                    Toast.LENGTH_SHORT
                ).show()
            }, { error ->
                // Find the position of the recipe to notify item change
                val currentPosition = recipesPagingAdapter.snapshot().indexOf(recipe)
                if (currentPosition != -1) { // Check if item exists in the current snapshot
                    recipe.isBookmarked = !isBookmarking // Revert local change
                    recipesPagingAdapter.notifyItemChanged(currentPosition)
                }
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            })
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipesRecyclerView.layoutManager = gridLayoutManager
        binding.recipesRecyclerView.adapter = recipesPagingAdapter
    }

    private fun setupListeners() {
        binding.createRecipeButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreateRecipeFormActivity::class.java))
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            recipesPagingAdapter.refresh() // Refresh data
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        recipesViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeBookmarkChanges() {
        recipesViewModel.bookmarkChanges.observe(viewLifecycleOwner) { isChanged ->
            if (isChanged && recipesViewModel.shouldRefreshOnResume) {
                recipesPagingAdapter.refresh() // Refresh only if returning from another activity
                recipesViewModel.resetBookmarkChangeFlag()
            }
        }
    }

    private fun observePagingData() {
        lifecycleScope.launch {
            recipesPagingAdapter.loadStateFlow.collectLatest { loadStates ->
                // Set SwipeRefreshLayout to show loading state
                binding.swipeRefreshLayout.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }
    }

    fun performSearch(query: String?) {
        query?.let {
            lifecycleScope.launch {
                recipesPagingAdapter.submitData(PagingData.empty()) // Clear current results
                recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                    val filteredPagingData = pagingData.filter { recipe ->
                        recipe.name.contains(query, ignoreCase = true) || recipe.description.contains(query, ignoreCase = true)
                    }
                    recipesPagingAdapter.submitData(filteredPagingData)
                }
            }
        }
    }

    fun resetSearch() {
        lifecycleScope.launch {
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
