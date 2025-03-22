package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.travel.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.travel.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

            bottomNav.setupWithNavController(navController)

            bottomNav.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.allPostsFragment -> {
                        if (navController.currentDestination?.id != R.id.allPostsFragment) {
                            navController.popBackStack(R.id.allPostsFragment, false)
                            navController.navigate(R.id.allPostsFragment)
                        }
                        true
                    }
                    R.id.createPostFragment -> {
                        if (navController.currentDestination?.id != R.id.createPostFragment) {
                            navController.popBackStack(R.id.createPostFragment, false)
                            navController.navigate(R.id.createPostFragment)
                        }
                        true
                    }
                    R.id.profileFragment -> {
                        if (navController.currentDestination?.id != R.id.profileFragment) {
                            navController.popBackStack(R.id.profileFragment, false)
                            navController.navigate(R.id.profileFragment)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
