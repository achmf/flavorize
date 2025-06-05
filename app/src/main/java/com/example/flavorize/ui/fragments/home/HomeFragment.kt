package com.example.flavorize.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavorize.databinding.FragmentHomeBinding
import com.example.flavorize.ui.activities.recipedetail.RecipeDetailActivity
import com.example.flavorize.ui.fragments.home.adapter.RecipeCardAdapter
import com.example.flavorize.ui.fragments.home.viewmodel.HomeFragmentViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance
    private val viewModel: HomeFragmentViewModel by viewModels()

    // Recipe adapter
    private lateinit var recipeAdapter: RecipeCardAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val imageSwitcherRunnable = object : Runnable {
        override fun run() {
            viewModel.updateCurrentImageIndex() // Update image index using ViewModel
            handler.postDelayed(this, 5000) // Switch image every 5 seconds
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageSwitcher()
        setupSearchView()
        setupRecyclerView()
        setupRefreshButton()
        observeViewModel()
    }

    private fun setupImageSwitcher() {
        binding.imageCarousel.setFactory {
            val imageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            imageView
        }

        // Start the image switching animation
        handler.post(imageSwitcherRunnable)
    }

    private fun setupSearchView() {
        // Set up the SearchView
        val searchView = binding.searchView

        // Configure SearchView behavior
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    viewModel.setSearchQuery(query)
                    // Update section title
                    binding.recipeSectionTitle.text = "Search Results"
                    // Hide keyboard
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // If search is cleared, show random recipes
                    if (viewModel.isSearchActive.value == true) {
                        viewModel.clearSearch()
                        binding.recipeSectionTitle.text = "Random Recipes"
                    }
                }
                return true
            }
        })

        // Handle close button in SearchView
        searchView.setOnCloseListener {
            viewModel.clearSearch()
            binding.recipeSectionTitle.text = "Random Recipes"
            false
        }
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with click listener
        recipeAdapter = RecipeCardAdapter { recipe ->
            // Navigate to RecipeDetailActivity with the selected recipe
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java).apply {
                putExtra("meal_id", recipe.id)
                putExtra("meal_name", recipe.name)
                putExtra("meal_category", recipe.category)
                putExtra("meal_area", recipe.area)
                putExtra("meal_instructions", recipe.instructions)
                putExtra("meal_image_url", recipe.imageUrl)
                putExtra("meal_youtube", recipe.youtubeUrl)
                putExtra("meal_ingredients", ArrayList(recipe.ingredients))
                putExtra("is_api_recipe", true) // Flag to identify it's from API
            }
            startActivity(intent)
        }

        // Setup the RecyclerView with a 2-column grid layout
        binding.recipesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recipeAdapter
        }
    }

    // Set up the refresh button
    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            // Call the refresh method in viewmodel
            viewModel.refreshRecipes()
        }
    }

    private fun observeViewModel() {
        // Observe images LiveData and set the first image
        viewModel.images.observe(viewLifecycleOwner) { images ->
            if (images.isNotEmpty()) {
                binding.imageCarousel.setImageResource(
                    images[viewModel.currentImageIndex.value ?: 0]
                )
            }
        }

        // Observe the current image index and update the ImageSwitcher
        viewModel.currentImageIndex.observe(viewLifecycleOwner) { index ->
            val images = viewModel.images.value ?: emptyList()
            if (images.isNotEmpty()) {
                binding.imageCarousel.setImageResource(images[index])
            }
        }

        // Observe dynamic info content and update the TextView
        viewModel.dynamicInfoContent.observe(viewLifecycleOwner) { content ->
            binding.infoContent.text = content
        }

        // Observe search mode changes
        viewModel.isSearchActive.observe(viewLifecycleOwner) { isActive ->
            binding.recipeSectionTitle.text = if (isActive) "Search Results" else "Random Recipes"
        }

        // Observe recipes from TheMealDB API
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)

            // Show message if no recipes are found
            if (recipes.isEmpty() && viewModel.error.value == null && viewModel.isLoading.value == false) {
                binding.errorText.text = "No recipes found. Try again later."
                binding.errorText.visibility = View.VISIBLE
            } else {
                binding.errorText.visibility = View.GONE
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingView.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Hide error when loading
            if (isLoading) {
                binding.errorText.visibility = View.GONE
            }
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.errorText.text = error
                binding.errorText.visibility = View.VISIBLE
            } else {
                binding.errorText.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(imageSwitcherRunnable) // Stop the handler when the fragment is destroyed
    }
}
