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
        observeViewModel()

        lifecycleScope.launch {
            recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                recipesPagingAdapter.submitData(pagingData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            recipesViewModel.startListeningForBookmarkChanges(userId)
            recipesViewModel.bookmarkUpdates.observe(viewLifecycleOwner) { updatedBookmarks ->
                lifecycleScope.launch {
                    recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
                        val updatedData = pagingData.map { recipe ->
                            recipe.copy(isBookmarked = updatedBookmarks.contains(recipe.id))
                        }
                        recipesPagingAdapter.submitData(updatedData)
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please log in to see bookmarks", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        recipesViewModel.stopListeningForBookmarkChanges()
    }

    private fun setupRecyclerView() {
        recipesPagingAdapter = RecipesPagingAdapter { recipe, isBookmarking, onComplete ->
            recipesViewModel.toggleBookmark(recipe, isBookmarking, {
                onComplete(true)
                Toast.makeText(
                    requireContext(),
                    if (isBookmarking) "Bookmarked!" else "Bookmark removed!",
                    Toast.LENGTH_SHORT
                ).show()
            }, { error ->
                onComplete(false)
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

    private fun observeViewModel() {
        recipesViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performSearch(query: String?) {
        query?.let {
            lifecycleScope.launch {
                recipesPagingAdapter.submitData(PagingData.empty())
                recipesViewModel.getPagedRecipes().collectLatest { pagingData ->
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