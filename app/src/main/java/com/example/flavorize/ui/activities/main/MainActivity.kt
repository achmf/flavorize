package com.example.flavorize.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.flavorize.R
import com.example.flavorize.databinding.ActivityMainBinding
import com.example.flavorize.ui.activities.bookmark.BookmarkedRecipesActivity
import com.example.flavorize.ui.activities.draft.DraftedRecipesActivity
import com.example.flavorize.ui.activities.main.viewmodel.MainViewModel
import com.example.flavorize.ui.activities.profile.ProfileActivity
import com.example.flavorize.ui.activities.auth.LoginActivity
import com.example.flavorize.ui.fragments.myrecipes.MyRecipesFragment
import com.example.flavorize.ui.fragments.recipes.RecipesFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // Redirect to login if user is not authenticated
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupDefaultToolbar() // Setup the default toolbar
        setupNavigation() // Setup navigation components
        setupSearchToolbar() // Configure the search toolbar
        setupDrawerHeader() // Setup the navigation drawer header
        observeViewModel() // Observe LiveData from the ViewModel
        setupSearchFunctionality() // Configure search functionality
    }

    private fun setupDefaultToolbar() {
        // Set the default toolbar
        setSupportActionBar(binding.toolbarDefault)
    }

    private fun setupSearchToolbar() {
        // Configure the search toolbar
        val searchToolbar = binding.toolbarSearch
        val searchView = searchToolbar.findViewById<SearchView>(R.id.searchView)

        // Back button listener
        searchToolbar.setNavigationOnClickListener {
            viewModel.hideSearchBar()
        }

        // Handle search actions
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@MainActivity, "Searching for: $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Add real-time suggestions here
                return false
            }
        })
    }

    private fun setupNavigation() {
        // Configure navigation components
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_recipes,
                R.id.navigation_my_recipes
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)
        binding.navView.setupWithNavController(navController)
    }

    private fun setupDrawerHeader() {
        // Configure the navigation drawer header
        val headerView = binding.navView.getHeaderView(0)
        val userAvatar = headerView.findViewById<ImageView>(R.id.headerUserAvatar)
        val userName = headerView.findViewById<TextView>(R.id.headerUserName)
        val userEmail = headerView.findViewById<TextView>(R.id.headerUserEmail)

        // Observe user details from the ViewModel
        viewModel.userAvatar.observe(this) { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(userAvatar)
        }

        viewModel.userName.observe(this) { name ->
            userName.text = name ?: "Unknown User"
        }

        viewModel.userEmail.observe(this) { email ->
            userEmail.text = email ?: "No Email"
        }

        // Handle profile navigation
        userAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Handle navigation item clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_bookmarked -> {
                    // Open bookmarked recipes
                    startActivity(Intent(this, BookmarkedRecipesActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.navigation_drafted -> {
                    // Open drafted recipes
                    startActivity(Intent(this, DraftedRecipesActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    // Handle other navigation items
                    menuItem.onNavDestinationSelected(navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun setupSearchFunctionality() {
        // Configure search behavior based on the selected destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    // Hide search icon on HomeFragment
                    invalidateOptionsMenu()
                }
                R.id.navigation_recipes -> {
                    // Show search on RecipesFragment
                    invalidateOptionsMenu()
                    binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
                        .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                // Pass search query to RecipesFragment
                                (getCurrentFragment() as? RecipesFragment)?.performSearch(query)
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                return false
                            }
                        })
                }
                R.id.navigation_my_recipes -> {
                    // Show search on MyRecipesFragment
                    invalidateOptionsMenu()
                    binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
                        .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                // Pass search query to MyRecipesFragment
                                (getCurrentFragment() as? MyRecipesFragment)?.performSearch(query)
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                return false
                            }
                        })
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe search bar visibility
        viewModel.isSearchBarVisible.observe(this, Observer { isVisible ->
            if (isVisible) {
                activateSearchBar()
            } else {
                closeSearchBar()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        // Hide search icon on HomeFragment
        menu?.findItem(R.id.action_search)?.isVisible =
            navController.currentDestination?.id != R.id.navigation_home

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle toolbar menu actions
        return when (item.itemId) {
            R.id.action_search -> {
                viewModel.showSearchBar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun activateSearchBar() {
        // Show search bar
        binding.toolbarDefault.visibility = View.GONE
        binding.toolbarSearch.visibility = View.VISIBLE

        val searchView = binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
        searchView.isIconified = false
        searchView.requestFocus()

        // Show keyboard
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            searchView,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    private fun closeSearchBar() {
        // Hide search bar
        binding.toolbarSearch.visibility = View.GONE
        binding.toolbarDefault.visibility = View.VISIBLE

        val searchView = binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
        searchView.setQuery("", false)
        searchView.clearFocus()

        // Reset search on the current fragment
        when (val currentFragment = getCurrentFragment()) {
            is RecipesFragment -> currentFragment.resetSearch()
            is MyRecipesFragment -> currentFragment.resetSearch()
        }

        // Hide keyboard
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            searchView.windowToken,
            0
        )
    }

    override fun onBackPressed() {
        // Close search bar if visible
        if (viewModel.isSearchBarVisible.value == true) {
            closeSearchBar()
            viewModel.hideSearchBar()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle navigation up action
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getCurrentFragment() =
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager?.fragments?.firstOrNull()
}
