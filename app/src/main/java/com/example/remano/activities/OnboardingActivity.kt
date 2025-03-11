package com.example.remano.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.remano.R
import com.example.remano.adapters.OnboardingAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextBtn: TextView
    private lateinit var prevBtn: TextView

    private val backgrounds = listOf(
        R.drawable.ic_onboarding1, // Replace with actual images
        R.drawable.ic_onboarding2,
        R.drawable.ic_onboarding3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        nextBtn = findViewById(R.id.btnNext)
        prevBtn = findViewById(R.id.btnPrev)

        viewPager.adapter = OnboardingAdapter(backgrounds)

        // Link ViewPager2 with TabLayout
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                prevBtn.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                nextBtn.text = if (position == backgrounds.size - 1) "Finish" else "Next"
            }
        })

        nextBtn.setOnClickListener {
            if (viewPager.currentItem < backgrounds.size - 1) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        prevBtn.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }
    }
}



