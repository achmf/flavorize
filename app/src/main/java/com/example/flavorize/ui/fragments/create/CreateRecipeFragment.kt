package com.example.flavorize.ui.fragments.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flavorize.databinding.FragmentCreateRecipeBinding
import com.example.flavorize.ui.createform.CreateRecipeFormActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreateRecipeFragment : Fragment() {
    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)

        val createRecipeButton: FloatingActionButton = binding.createRecipeButton
        createRecipeButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateRecipeFormActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}