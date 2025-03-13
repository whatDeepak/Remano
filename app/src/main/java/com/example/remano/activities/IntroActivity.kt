package com.example.remano.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.remano.R
import com.example.remano.firebase.FirestoreClass
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class IntroActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)

        val btnLoginIntro: Button = findViewById(R.id.btn_login_intro)
        val btnGoogleIntro: Button = findViewById(R.id.btn_google_intro)

        btnLoginIntro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        btnGoogleIntro.setOnClickListener {
            FirestoreClass().signInWithGoogle(this)
        }

    }

    fun userRegisteredSuccessIntro() {

        Toast.makeText(
            this@IntroActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */

        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
        finish()
    }
}