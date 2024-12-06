package com.example.flavorize.ui.fragments.myrecipes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.DialogConfirmMyrecipesDeleteBinding
import com.example.flavorize.databinding.FragmentMyRecipesBinding
import com.example.flavorize.ui.activities.editrecipe.EditRecipeActivity
import com.example.flavorize.ui.fragments.myrecipes.paging.MyRecipesPagingAdapter
import com.example.flavorize.ui.fragments.myrecipes.viewmodel.MyRecipesFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyRecipesFragment : Fragment() {
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyRecipesFragmentViewModel by viewModels()
    private lateinit var myRecipesPagingAdapter: MyRecipesPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView for displaying recipes
        setupRecyclerView()
        // Observe bookmark changes
        observeBookmarkUpdates()
        // Load user recipes
        loadUserRecipes()
    }

    private fun setupRecyclerView() {
        myRecipesPagingAdapter = MyRecipesPagingAdapter(
            onEditRecipe = { navigateToEditRecipe(it) }, // Handle edit recipe action
            onDeleteRecipe = { confirmDeleteRecipe(it) }, // Handle delete recipe action
            onBookmarkToggle = { recipe, isBookmarking, onComplete ->
                // Handle bookmark toggle
                viewModel.toggleBookmark(recipe, isBookmarking, {
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
        )
        // Set layout manager and adapter for RecyclerView
        binding.myRecipesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.myRecipesRecyclerView.adapter = myRecipesPagingAdapter
    }

    private fun loadUserRecipes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Fetch user recipes
            lifecycleScope.launch {
                viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                    myRecipesPagingAdapter.submitData(pagingData)
                }
            }

            // Handle loading states for RecyclerView
            lifecycleScope.launch {
                myRecipesPagingAdapter.loadStateFlow.collectLatest { loadStates ->
                    val isLoading = loadStates.refresh is androidx.paging.LoadState.Loading
                    val isListEmpty = !isLoading && myRecipesPagingAdapter.itemCount == 0

                    binding.emptyStateTextView.visibility =
                        if (isListEmpty) View.VISIBLE else View.GONE

                    binding.myRecipesRecyclerView.visibility =
                        if (isListEmpty) View.GONE else View.VISIBLE
                }
            }
        } else {
            // Show error if user is not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeBookmarkUpdates() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Start listening for bookmark changes
            viewModel.startListeningForBookmarkChanges(userId)
            viewModel.bookmarkUpdates.observe(viewLifecycleOwner) { updatedBookmarks ->
                lifecycleScope.launch {
                    viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                        // Update recipes with the latest bookmark states
                        val updatedData = pagingData.map { recipe ->
                            recipe.copy(isBookmarked = updatedBookmarks.contains(recipe.id))
                        }
                        myRecipesPagingAdapter.submitData(updatedData)
                    }
                }
            }
        }
    }

    private fun navigateToEditRecipe(recipe: Recipe) {
        // Navigate to the Edit Recipe activity
        val intent = Intent(requireContext(), EditRecipeActivity::class.java).apply {
            putExtra("recipe", recipe)
        }
        startActivity(intent)
    }

    private fun confirmDeleteRecipe(recipe: Recipe) {
        // Show a confirmation dialog before deleting a recipe
        val binding = DialogConfirmMyrecipesDeleteBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        // Handle delete confirmation
        binding.yesButton.setOnClickListener {
            deleteRecipe(recipe)
            dialog.dismiss()
        }

        // Handle delete cancellation
        binding.noButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set transparent background for dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun deleteRecipe(recipe: Recipe) {
        // Perform delete action
        val firestoreRepository = FirestoreRepository()
        lifecycleScope.launch {
            val result = firestoreRepository.deleteRecipe(recipe.id)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                myRecipesPagingAdapter.refresh()
            } else {
                Toast.makeText(requireContext(), "Failed to delete recipe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performSearch(query: String?) {
        query?.let { searchText ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Perform search filtering on recipes
                lifecycleScope.launch {
                    viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                        val filteredData = pagingData.filter { recipe ->
                            recipe.name.contains(searchText, ignoreCase = true) ||
                                    recipe.description.contains(searchText, ignoreCase = true)
                        }
                        myRecipesPagingAdapter.submitData(filteredData)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetSearch() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Reset search and show all recipes
            lifecycleScope.launch {
                viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                    myRecipesPagingAdapter.submitData(pagingData)
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Start listening for bookmark changes
            viewModel.startListeningForBookmarkChanges(userId)
            viewModel.bookmarkUpdates.observe(viewLifecycleOwner) { updatedBookmarks ->
                lifecycleScope.launch {
                    viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                        val updatedData = pagingData.map { recipe ->
                            recipe.copy(isBookmarked = updatedBookmarks.contains(recipe.id))
                        }
                        myRecipesPagingAdapter.submitData(updatedData)
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop listening for recipe and bookmark changes
        viewModel.stopListeningForRecipeChanges()
        viewModel.stopListeningForBookmarkChanges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding when view is destroyed
        _binding = null
    }
}
