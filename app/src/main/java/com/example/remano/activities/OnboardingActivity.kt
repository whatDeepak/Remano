package com.example.remano.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.remano.R
import com.example.remano.adapters.OnboardingAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextBtn: TextView
    private lateinit var prevBtn: TextView
    private lateinit var skipBtn: TextView
    private lateinit var getStartedBtn: TextView

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
        skipBtn = findViewById(R.id.btnSkip)
        getStartedBtn = findViewById(R.id.btnGetStarted)

        viewPager.adapter = OnboardingAdapter(backgrounds)

        // Apply smooth scrolling effect
        slowDownViewPager(viewPager)

        // Link ViewPager2 with TabLayout
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == backgrounds.size - 1) {
                    // Hide everything except "Get Started" button
                    prevBtn.visibility = View.INVISIBLE
                    nextBtn.visibility = View.INVISIBLE
                    skipBtn.visibility = View.INVISIBLE
                    tabLayout.visibility = View.INVISIBLE

                    getStartedBtn.visibility = View.VISIBLE // Show Get Started button
                } else {
                    // Show navigation buttons normally
                    prevBtn.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                    nextBtn.visibility = View.VISIBLE
                    skipBtn.visibility = View.VISIBLE
                    tabLayout.visibility = View.VISIBLE

                    getStartedBtn.visibility = View.GONE // Hide Get Started button
                }
            }
        })

        nextBtn.setOnClickListener {
            if (viewPager.currentItem < backgrounds.size - 1) {
                smoothScrollToPage(viewPager, viewPager.currentItem + 1) // Slow Next Transition
            }
        }

        prevBtn.setOnClickListener {
            if (viewPager.currentItem > 0) {
                smoothScrollToPage(viewPager, viewPager.currentItem - 1) // Slow Prev Transition
            }
        }

        skipBtn.setOnClickListener {
            smoothScrollToPage(viewPager, backgrounds.size - 1)
        }

        getStartedBtn.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }
    }

    /**
     * Function to slow down ViewPager2 scrolling speed for swipe gestures
     */
    private fun slowDownViewPager(viewPager: ViewPager2) {
        try {
            val recyclerView = viewPager.getChildAt(0) as RecyclerView
            recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER // Disable overscroll effect

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val smoothScroller = object : LinearSmoothScroller(viewPager.context) {
                override fun getHorizontalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                    return 100f / displayMetrics.densityDpi // Matches button transition speed (800ms)
                }
            }

            // Apply custom fling behavior to slow down swipe speed
            recyclerView.setOnFlingListener(object : RecyclerView.OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    val targetPage = viewPager.currentItem + if (velocityX > 0) 1 else -1
                    if (targetPage in 0 until viewPager.adapter!!.itemCount) {
                        smoothScroller.targetPosition = targetPage
                        layoutManager.startSmoothScroll(smoothScroller)
                        return true
                    }
                    return false
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Function to smooth transition pages with 800ms duration
     */
    private fun smoothScrollToPage(viewPager: ViewPager2, targetPage: Int) {
        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val smoothScroller = object : LinearSmoothScroller(viewPager.context) {
            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                return 100f/ displayMetrics.densityDpi // Adjust speed (800ms transition)
            }
        }
        smoothScroller.targetPosition = targetPage
        layoutManager.startSmoothScroll(smoothScroller)
    }
}