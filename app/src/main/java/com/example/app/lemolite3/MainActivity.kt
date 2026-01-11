package com.example.app.lemolite3

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.app.lemolite3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val createPostFragment = CreatePostFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBar()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            .add(R.id.fragment_container, createPostFragment, "CREATE").hide(createPostFragment)
            .add(R.id.fragment_container, homeFragment, "HOME")
            .commit()

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_create -> switchFragment(createPostFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
            }
            true
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.WHITE

        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
    }
}