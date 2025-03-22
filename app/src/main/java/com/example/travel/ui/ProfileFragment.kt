package com.example.travel.ui

import android.util.Log
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.data.AppDatabase
import com.example.travel.repository.PostRepository
import com.example.travel.repository.UserRepository
import com.example.travel.adapters.ProfilePostAdapter
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.example.travel.viewmodel.UserViewModel
import com.example.travel.viewmodel.UserViewModelFactory
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import com.example.travel.auth.LoginActivity
import com.example.travel.data.CloudinaryModel
import com.example.travel.data.firebase.FirebaseService

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var auth: FirebaseAuth
    val firebaseService = FirebaseService()
    val cloudinaryModel = CloudinaryModel()

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var profileImage: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_user_posts)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        val db = AppDatabase.getDatabase(requireContext())
        val userEmail = auth.currentUser?.email ?: ""
        val tvUsername = view.findViewById<TextView>(R.id.tv_username)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)
        profileImage = view.findViewById(R.id.profile_image)

        recyclerView.layoutManager = layoutManager

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(db.userDao(), FirebaseFirestore.getInstance()))
        )[UserViewModel::class.java]

        postViewModel = ViewModelProvider(
            this,
            PostViewModelFactory(PostRepository(db.postDao(),firebaseService ), cloudinaryModel)
        )[PostViewModel::class.java]
        Log.d("ProfileFragment", "userEmail: $userEmail")

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadProfileImage(it)
            }
        }
        // Fetch user info from Room (synced with Firestore)
        userViewModel.getUserByEmail(userEmail).observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUsername.text = it.username
                tvEmail.text = it.email

                // Load profile picture from Firebase URL saved earlier
                Glide.with(this@ProfileFragment)
                    .load(it.profilePicture)
                    .placeholder(R.drawable.ic_profile) // fallback image
                    .circleCrop()
                    .into(profileImage)

                // Load user posts
                postViewModel.getAllPosts().observe(viewLifecycleOwner) { allPosts ->
                    Log.d("ProfileFragment", "All posts: $allPosts")
                    allPosts.forEach { post ->
                        Log.d("ProfileFragment", "Post owner: ${post.owner}")
                    }
                    val userPosts = allPosts.filter { post -> post.owner == userEmail }
                    Log.d("ProfileFragment", "User posts: $userPosts")
                    recyclerView.adapter = ProfilePostAdapter(userPosts)
                }
            }
        }

        val editButton = view.findViewById<Button>(R.id.btn_edit_profile)
        editButton.setOnClickListener {
            showEditProfileDialog()
        }
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.et_edit_username)
        val ivProfilePic = dialogView.findViewById<ImageView>(R.id.iv_edit_profile_image)

        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseFirestore.getInstance().collection("users").document(user?.uid ?: "")

        userRef.get().addOnSuccessListener { document ->
            val profilePicUrl = document?.getString("profilePicture")
            if (profilePicUrl != null) {
                Glide.with(requireContext()).load(profilePicUrl)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(ivProfilePic)
            }
        }
        ivProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")  // Launch the image picker
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etUsername.text.toString()
                val newProfilePicUrl = ivProfilePic.tag as? Uri
                if (newUsername != null) {
                    updateUsernameInFirebase(newUsername)
                }
                if (newProfilePicUrl != null) {
                    uploadProfileImage(newProfilePicUrl)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun uploadProfileImage(uri: Uri) {
        val uniqueName = "profile_image_${System.currentTimeMillis()}"
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        cloudinaryModel.uploadImage(bitmap, uniqueName, { imageUrl ->
            // onSuccess callback
            imageUrl?.let {
                // Update Firebase with the new profile image URL
                updateProfilePictureInFirebase(it)
                syncUserFromFirebase()
            }
        }, { errorMessage ->
            // onError callback
            Toast.makeText(requireContext(), "Error uploading image: $errorMessage", Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateProfilePictureInFirebase(url: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseFirestore.getInstance().collection("users").document(user?.uid ?: "")

        userRef.update("profilePicture", url)
            .addOnSuccessListener {
                // Sync user data with Room after updating Firebase
                syncUserFromFirebase()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateUsernameInFirebase(username: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseFirestore.getInstance().collection("users").document(user?.uid ?: "")

        userRef.update("username", username)
            .addOnSuccessListener {
                syncUserFromFirebase()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating username: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun syncUserFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userViewModel.syncUser(userId) // Sync Room with Firebase
    }
}
