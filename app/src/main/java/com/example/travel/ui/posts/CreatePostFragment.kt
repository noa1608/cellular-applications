package com.example.travel.ui.posts

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travel.R
import com.example.travel.data.Post
import com.example.travel.repository.PostRepository
import com.example.travel.data.AppDatabase
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.example.travel.utils.savePostImageToDirectory
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.example.travel.data.CloudinaryModel
import com.example.travel.data.firebase.FirebaseService


class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    val firebaseService = FirebaseService()
    val cloudinaryModel = CloudinaryModel()
    private var imageUri: Uri? = null
    private lateinit var postViewModel: PostViewModel
    private lateinit var imageView: ImageView
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                imageView.setImageURI(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao, firebaseService)
        val postViewModelFactory = PostViewModelFactory(postRepository, cloudinaryModel)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)
        val postTitle = view.findViewById<EditText>(R.id.et_post_title)
        val postDescription = view.findViewById<EditText>(R.id.et_post_description)
        val saveButton = view.findViewById<Button>(R.id.btn_save_post)
        val selectImageButton = view.findViewById<Button>(R.id.btn_select_image)
        imageView = view.findViewById(R.id.iv_post_image)

        selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val title = postTitle.text.toString().trim()
            val description = postDescription.text.toString().trim()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val owner = currentUser?.uid ?: "unknown_user"
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imagePath = savePostImageToDirectory(imageUri!!, requireContext())

            if (imagePath == null) {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPost = Post(
                title = title,
                content = description,
                imagePath = imagePath,
                owner = owner
            )
            Log.d("CreatePostFragment", "Saving post: Title: $title, Description: $description, ImagePath: $imagePath")


            postViewModel.createPostWithImage(requireContext(), newPost, imageUri!!, { postId ->
                Toast.makeText(requireContext(), "Post saved successfully!", Toast.LENGTH_SHORT).show()
                val action = CreatePostFragmentDirections.actionCreatePostFragmentToSinglePostFragment(postId)
                findNavController().navigate(action)
            }, { error ->
                Toast.makeText(requireContext(), "Failed to save post: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }
}
