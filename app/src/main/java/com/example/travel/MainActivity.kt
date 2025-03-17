package com.example.travel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.travel.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.travel.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is authenticated with Firebase
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // If no user is authenticated, navigate to the LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)

            // Set up NavHostFragment for navigation
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
        }
    }
}
