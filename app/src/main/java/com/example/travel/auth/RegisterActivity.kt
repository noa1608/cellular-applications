package com.example.travel.auth

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.travel.R
import com.example.travel.data.AppDatabase
import com.example.travel.data.User
import com.example.travel.repository.UserRepository
import com.example.travel.viewmodel.UserViewModel
import com.example.travel.viewmodel.UserViewModelFactory
import com.example.travel.data.CloudinaryModel // Import the CloudinaryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var cloudinaryModel: CloudinaryModel
    private var imageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                Toast.makeText(this, "Profile image selected!", Toast.LENGTH_SHORT).show()
                Log.d("RegisterActivity", "Selected Image Uri: $imageUri")
            }
        }
    fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
        return try {
            val inputStream: InputStream = contentResolver.openInputStream(this) ?: return null
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        cloudinaryModel = CloudinaryModel()

        // Initialize ViewModel
        val userRepository = UserRepository(
            AppDatabase.getDatabase(this).userDao(),
            firestore, storage
        )
        val userViewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val selectProfileImageButton = findViewById<Button>(R.id.selectProfileImageButton)

        // Image picker button
        selectProfileImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Register button
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

            // Check if Uri is accessible
            try {
                contentResolver.openInputStream(imageUri!!)?.close()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert Uri to Bitmap
            val bitmap = imageUri!!.toBitmap(contentResolver)

            if (bitmap != null) {
                Log.d("RegisterActivity", "Bitmap converted successfully: Width: ${bitmap.width}, Height: ${bitmap.height}")
                // Register with Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""
                            Log.d("RegisterActivity", "User created: $userId")


                            cloudinaryModel.uploadImage(bitmap, username,
                                onSuccess = { imageUrl ->
                                    Log.d("Cloudinary", "Image uploaded successfully: $imageUrl")
                                    if (imageUrl != null) {
                                        val user = User(
                                            id = userId,
                                            email = email,
                                            username = username,
                                            profilePicture = imageUrl
                                        )

                                        userViewModel.createUser(user)
                                        userViewModel.createUserStatus.observe(this) { result ->
                                            result.onSuccess {
                                                // User created & synced successfully
                                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, LoginActivity::class.java))
                                                finish()                                            }
                                            result.onFailure { error ->
                                                // Handle error
                                                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Log.e("Cloudinary", "Image upload failed: No image URL")
                                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { error ->
                                    Log.e("Cloudinary", "Image upload failed: $error")
                                    Toast.makeText(this, "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                                })
                        } else {
                            Log.e("RegisterActivity", "Registration failed: ${task.exception?.message}")
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("RegisterActivity", "Failed to convert Uri to Bitmap")
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
