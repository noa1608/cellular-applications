package com.example.travel.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.travel.R
import com.example.travel.data.AppDatabase
import com.example.travel.data.User
import com.example.travel.repository.UserRepository
import com.example.travel.ui.viewmodel.UserViewModel
import com.example.travel.ui.viewmodel.UserViewModelFactory
import com.example.travel.utils.saveProfileImageToDirectory
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private var imageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                Toast.makeText(this, "Profile image selected!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UserViewModel with Factory
        val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
        val userViewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]

        // UI Elements
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val selectProfileImageButton = findViewById<Button>(R.id.selectProfileImageButton)

        // Select Profile Image button
        selectProfileImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Register Button logic
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save image locally
            val profileImagePath = saveProfileImageToDirectory(imageUri!!, this)
            if (profileImagePath == null) {
                Toast.makeText(this, "Failed to save profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Auth Register
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save user to Room DB
                        val user = User(
                            email = email,
                            username = username,
                            profilePictureUrl = profileImagePath
                        )
                        userViewModel.insertUser(user)
                        android.util.Log.d("RegisterActivity", "User saved to Room DB: $user")

                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
