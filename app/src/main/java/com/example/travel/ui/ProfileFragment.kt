package com.example.travel.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var profileImage:ImageView
    private lateinit var editButton: ImageView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                // Show the image
                Glide.with(this)
                    .load(uri)
                    .into(profileImage)
                // TODO: Save uri.toString() to Room database as profile picture path
            }
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_user_posts)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        val db = AppDatabase.getDatabase(requireContext())
        val userEmail = auth.currentUser?.email ?: ""
        val tvUsername = view.findViewById<TextView>(R.id.tv_username)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)

        recyclerView.layoutManager = layoutManager

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(db.userDao(), FirebaseFirestore.getInstance()))
        )[UserViewModel::class.java]

        postViewModel = ViewModelProvider(
            this,
            PostViewModelFactory(PostRepository(db.postDao()))
        )[PostViewModel::class.java]


        userViewModel.getUserByEmail(userEmail).observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUsername.text = it.username
                tvEmail.text = it.email

                it.profilePicture.let { path ->
                    Glide.with(this@ProfileFragment)
                        .load(path)
                        .circleCrop()
                        .into(profileImage)
                }

                postViewModel.getAllPosts().observe(viewLifecycleOwner) { allPosts ->
                    val userPosts = allPosts.filter { post -> post.owner == userEmail }
                    recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    recyclerView.adapter = ProfilePostAdapter(userPosts)
                }
            }
        }

        val editButton = view.findViewById<Button>(R.id.btn_edit_profile)
        editButton.setOnClickListener {
            showEditProfileDialog()
        }
    }
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.et_edit_username)
        val ivProfilePic = dialogView.findViewById<ImageView>(R.id.iv_edit_profile_image)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etUsername.text.toString()
                // Save username to Room and update UI
                // Optional: handle image upload logic here
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
