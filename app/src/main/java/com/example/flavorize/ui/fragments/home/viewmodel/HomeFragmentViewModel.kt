package com.example.flavorize.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flavorize.ui.fragments.home.data.HomeContent

class HomeFragmentViewModel : ViewModel() {

    // Simulasi konten untuk Home
    private val _content = MutableLiveData<List<HomeContent>>()
    val content: LiveData<List<HomeContent>> get() = _content

    init {
        // Data dummy untuk ditampilkan
        _content.value = listOf(
            HomeContent("Welcome to Flavorize", "Explore a world of recipes and culinary ideas."),
            HomeContent("New Recipes", "Check out the latest recipes added by our community."),
            HomeContent("Popular Recipes", "Discover the most-loved recipes by users."),
        )
    }
}
