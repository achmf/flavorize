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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.FirestoreRepository
import com.example.flavorize.data.Recipe
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

        setupRecyclerView()
        setupSwipeToRefresh()
        loadUserRecipes()
    }

    private fun setupRecyclerView() {
        myRecipesPagingAdapter = MyRecipesPagingAdapter(
            onEditRecipe = { navigateToEditRecipe(it) },
            onDeleteRecipe = { confirmDeleteRecipe(it) }
        )
        binding.myRecipesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.myRecipesRecyclerView.adapter = myRecipesPagingAdapter
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            myRecipesPagingAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadUserRecipes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            lifecycleScope.launch {
                viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                    myRecipesPagingAdapter.submitData(pagingData)
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    fun performSearch(query: String?) {
        query?.let { searchText ->
            val userId = viewModel.firestoreRepository.getCurrentUserId()
            if (userId != null) {
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
        val userId = viewModel.firestoreRepository.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                viewModel.getPagedMyRecipes(userId).collectLatest { pagingData ->
                    myRecipesPagingAdapter.submitData(pagingData)
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditRecipe(recipe: Recipe) {
        val intent = Intent(requireContext(), EditRecipeActivity::class.java).apply {
            putExtra("recipe", recipe)
        }
        startActivity(intent)
    }

    private fun confirmDeleteRecipe(recipe: Recipe) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Yes") { _, _ -> deleteRecipe(recipe) }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    private fun deleteRecipe(recipe: Recipe) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
