package com.example.remano.fragments

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.remano.R
import com.example.remano.activities.IntroActivity
import com.example.remano.databinding.FragmentHomeBinding
import com.example.remano.firebase.FirestoreClass
import com.example.remano.model.User
import java.util.Calendar


class HomeFragment: Fragment() {

    // A global variable for User Name
    private lateinit var mUserName: String
    private val firestoreClass = FirestoreClass()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        firestoreClass.loadUserData(requireContext())

        val greetingTextView: TextView = binding.greetingText
        // Load custom typeface from the "assets" folder
        val customTypeface = Typeface.createFromAsset(requireContext().assets, "alchemist_regular.ttf")
        // Apply custom typeface to the button
        greetingTextView.typeface = customTypeface

        greetingMessage()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // Handle sign-out when button is clicked
//        binding.btnSignout.setOnClickListener {
//            signOutUser()
//        }


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

    /**
     * A function to get the current user details from firebase.
     */
    fun updateUserDetails(user: User) {
        mUserName = user.name

        // The instance of the user image of the navigation view.
        val navUserImage = binding.userAvatar

        Log.e("UserData", "User Image URL: ${user.image}")

        Glide
            .with(requireContext())
            .load(user.image.takeIf { it.isNotEmpty() } ?: R.drawable.ic_user_place_holder)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)

    }

    private fun greetingMessage() {
        val greetingTextView: TextView = binding.greetingText
        // Call the function to get user data
        FirestoreClass().getUserData(
            onSuccess = { user ->
                // User data retrieved successfully
                val greeting = getGreetingMessage(user.name)
                greetingTextView.text = greeting

                // Find the index of the comma
                val commaIndex = greeting.indexOf(",")

                // Create a StringBuilder to build the modified string
                val modifiedString = StringBuilder(greeting)

                // Insert a newline character after the comma
                modifiedString.insert(commaIndex + 1, "\n")

                // Create a SpannableString
                val spannableString = SpannableString(modifiedString.toString())

                // Set the final SpannableString to the TextView
                greetingTextView.text = spannableString
            },
            onFailure = {
                // Handle failure to retrieve user data
            }
        )
    }

    private fun getGreetingMessage(userName: String): String {
        val cal = Calendar.getInstance()
        val timeOfDay = cal[Calendar.HOUR_OF_DAY]

        return when (timeOfDay) {
            in 4..11 -> "Good Morning,$userName!"
            in 12..15 -> "Good Afternoon,$userName!"
            in 16..20 -> "Good Evening,$userName!"
            else -> "Good Night,$userName!"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}