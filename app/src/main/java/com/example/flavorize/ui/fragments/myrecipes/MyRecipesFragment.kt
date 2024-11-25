package com.example.flavorize.ui.fragments.myrecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.data.Recipe
import com.example.flavorize.databinding.FragmentMyRecipesBinding
import com.example.flavorize.ui.fragments.myrecipes.viewmodel.MyRecipesFragmentViewModel

class MyRecipesFragment : Fragment() {
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyRecipesFragmentViewModel by viewModels()
    private lateinit var myRecipesAdapter: MyRecipesAdapter

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
        observeViewModel()

        viewModel.fetchMyRecipes()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        myRecipesAdapter = MyRecipesAdapter(
            allRecipes = listOf(),
            onDelete = { recipe -> handleDeleteRecipe(recipe) }
        )
        binding.myRecipesRecyclerView.layoutManager = gridLayoutManager
        binding.myRecipesRecyclerView.adapter = myRecipesAdapter
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshMyRecipes()
        }
    }

    private fun observeViewModel() {
        viewModel.myRecipes.observe(viewLifecycleOwner) { recipes ->
            myRecipesAdapter.updateRecipes(recipes)
            binding.swipeRefreshLayout.isRefreshing = false // Stop refreshing animation
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide ProgressBar based on loading state
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false // Stop refreshing animation
            }
        }
    }

    private fun handleDeleteRecipe(recipe: Recipe) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Recipe")
        builder.setMessage("Are you sure you want to delete the recipe \"${recipe.name}\"?")
        builder.setPositiveButton("Yes") { _, _ ->
            viewModel.deleteRecipe(
                recipeId = recipe.id,
                onSuccess = {
                    Toast.makeText(requireContext(), "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun performSearch(query: String?) {
        query?.let {
            viewModel.myRecipes.value?.let { recipes ->
                val filteredRecipes = recipes.filter { recipe ->
                    recipe.name.contains(query, ignoreCase = true) || recipe.description.contains(query, ignoreCase = true)
                }
                myRecipesAdapter.updateRecipes(filteredRecipes)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
