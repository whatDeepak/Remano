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
import com.example.remano.firebase.FirestoreClass

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
        val userId = FirestoreClass().getCurrentUserID()
        if (userId == null) {
            // User is signed out, go to the intro screen
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        } else {
            // User is signed in, proceed normally
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
