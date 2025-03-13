package com.example.remano.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.remano.activities.IntroActivity
import com.example.remano.databinding.FragmentHomeBinding
import com.example.remano.firebase.FirestoreClass

class HomeFragment: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle sign-out when button is clicked
        binding.btnSignout.setOnClickListener {
            signOutUser()
        }
    }

    private fun signOutUser() {
        FirestoreClass().signOut(requireActivity())

        // Small delay to ensure sign-out completes
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(requireContext(), IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}