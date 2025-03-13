package com.example.remano.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.remano.R
import com.example.remano.firebase.FirestoreClass
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSignOut: TextView = findViewById(R.id.btn_signout)
        btnSignOut.setOnClickListener {
            signOutUser()
        }
    }

    private fun signOutUser() {
        FirestoreClass().signOut(this@MainActivity)

        // Wait a bit to ensure credentials are cleared before redirecting
        android.os.Handler(mainLooper).postDelayed({
            val intent = Intent(this@MainActivity, IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 500) // Small delay to ensure sign-out process completes
    }
}