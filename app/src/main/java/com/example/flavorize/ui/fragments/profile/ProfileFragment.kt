package com.example.flavorize.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.flavorize.databinding.FragmentProfileBinding
import com.example.flavorize.ui.auth.LoginActivity
import com.example.flavorize.ui.fragments.profile.viewmodel.ProfileFragmentViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.loadUserProfile()

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }
    }


    private fun observeViewModel() {
        viewModel.userAvatar.observe(viewLifecycleOwner) { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(binding.userAvatarImageView)
        }

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.userNameTextView.text = name ?: "Unknown User"
        }

        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.userEmailTextView.text = email ?: "No Email"
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAccount() {
        viewModel.deleteAccount { isSuccessful, errorMessage ->
            if (isSuccessful) {
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
