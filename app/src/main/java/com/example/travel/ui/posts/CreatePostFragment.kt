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
import com.example.travel.utils.saveImageToSharedDirectory
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private var imageUri: Uri? = null  // Nullable to avoid crashes
    private lateinit var postViewModel: PostViewModel
    private lateinit var imageView: ImageView
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                imageView.setImageURI(it)
            }
        }
    private val getResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    imageUri = it
                    imageView.setImageURI(it) // Display selected image
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao)
        val postViewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)

        // Get UI references
        val postTitle = view.findViewById<EditText>(R.id.et_post_title)
        val postDescription = view.findViewById<EditText>(R.id.et_post_description)
        val saveButton = view.findViewById<Button>(R.id.btn_save_post)
        val selectImageButton = view.findViewById<Button>(R.id.btn_select_image)
        imageView = view.findViewById(R.id.iv_post_image)

        // Open gallery to select an image
        selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Save post when the save button is clicked
        saveButton.setOnClickListener {
            val title = postTitle.text.toString().trim()
            val description = postDescription.text.toString().trim()

            // Validate inputs
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if an image is selected
            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the image to shared directory (internal or external storage)
            val imagePath = saveImageToSharedDirectory(imageUri!!, requireContext())

            // If saving the image failed, show an error
            if (imagePath == null) {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a Post object
            val newPost = Post(
                title = title,
                content = description,
                imagePath = imagePath,
                owner = currentUserId
            )
            Log.d("CreatePostFragment", "Saving post: Title: $title, Description: $description, ImagePath: $imagePath")
            // Save the post using ViewModel
            postViewModel.insertPost(newPost)

            // Observe the result from ViewModel
            postViewModel.postInsertResult.observe(viewLifecycleOwner) { newPostId ->
                if (newPostId > 0) { // ✅ Check if a valid ID is returned
                    Toast.makeText(requireContext(), "Post saved successfully!", Toast.LENGTH_SHORT).show()

                    val bundle = Bundle().apply {
                        putLong("postId", newPostId) // ✅ Pass the correct post ID
                    }
                    findNavController().navigate(R.id.singlePostFragment, bundle)
                } else {
                    Toast.makeText(requireContext(), "Failed to save post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

