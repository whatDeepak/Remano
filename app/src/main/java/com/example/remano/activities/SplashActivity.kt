package com.example.remano.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.VideoView
import com.example.remano.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoPath = "android.resource://$packageName/${R.raw.splash_video}"
        val videoUri = videoPath.toUri()
        videoView.setVideoURI(videoUri)
        videoView.start()

        videoView.setOnCompletionListener {
            navigateToOnboardingActivity() // Move to onboarding after video ends
        }

    }

    private fun navigateToOnboardingActivity() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish() // Prevent user from navigating back to splash
    }

//        Handler().postDelayed({
//            // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
//            // we will redirect him to MainScreen or else to the Intro Screen as it was before.
//
//            // Get the current user id
//            val currentUserID = FirestoreClass().getCurrentUserID()
//            // Start the Intro Activity
//
//            if (currentUserID.isNotEmpty()) {
//                // Start the Main Activity
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            } else {
//                // Start the Intro Activity
//                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
//            }
//            finish() // Call this when your activity is done and should be closed.
//        }, 2500) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.


}
