package com.example.flavorize

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
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.flavorize.databinding.ActivityMainBinding
import com.example.flavorize.ui.auth.LoginActivity
import com.example.flavorize.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupDefaultToolbar()
        setupNavigation()
        setupSearchToolbar()
        setupDrawerHeader()
        observeViewModel()
    }

    private fun setupDefaultToolbar() {
        setSupportActionBar(binding.toolbarDefault)
    }

    private fun setupSearchToolbar() {
        val searchToolbar = binding.toolbarSearch
        val searchView = searchToolbar.findViewById<SearchView>(R.id.searchView)

        // Back button listener
        searchToolbar.setNavigationOnClickListener {
            viewModel.hideSearchBar()
        }

        // SearchView query listeners
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@MainActivity, "Searching for: $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Real-time filtering or suggestions
                return false
            }
        })
    }

    private fun setupNavigation() {
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
        val headerView = binding.navView.getHeaderView(0)
        val userAvatar = headerView.findViewById<ImageView>(R.id.headerUserAvatar)
        val userName = headerView.findViewById<TextView>(R.id.headerUserName)
        val userEmail = headerView.findViewById<TextView>(R.id.headerUserEmail)

        viewModel.userAvatar.observe(this, Observer { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(userAvatar)
        })

        viewModel.userName.observe(this, Observer { name ->
            userName.text = name ?: "Unknown User"
        })

        viewModel.userEmail.observe(this, Observer { email ->
            userEmail.text = email ?: "No Email"
        })

        // Add onClickListener for profile navigation
        userAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.isSearchBarVisible.observe(this, Observer { isVisible ->
            if (isVisible) {
                activateSearchBar()
            } else {
                closeSearchBar()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                viewModel.showSearchBar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun activateSearchBar() {
        binding.toolbarDefault.visibility = View.GONE
        binding.toolbarSearch.visibility = View.VISIBLE

        val searchView = binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
        searchView.visibility = View.VISIBLE
        searchView.isIconified = false // Ensure SearchView is not collapsed
        searchView.requestFocus() // Focus on SearchView

        // Show keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeSearchBar() {
        binding.toolbarSearch.visibility = View.GONE
        binding.toolbarDefault.visibility = View.VISIBLE

        val searchView = binding.toolbarSearch.findViewById<SearchView>(R.id.searchView)
        searchView.setQuery("", false) // Clear input text
        searchView.clearFocus() // Remove focus from SearchView

        // Hide keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchView.windowToken, 0)
    }

    override fun onBackPressed() {
        if (viewModel.isSearchBarVisible.value == true) {
            viewModel.hideSearchBar()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
