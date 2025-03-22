package com.example.travel.ui.posts

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.data.AppDatabase
import com.example.travel.data.Post
import com.example.travel.repository.PostRepository
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.example.travel.data.CloudinaryModel
import com.example.travel.data.firebase.FirebaseService
import androidx.activity.result.contract.ActivityResultContracts

class EditPostFragment : Fragment(R.layout.fragment_edit_post) {

    private var imageUri: Uri? = null
    private lateinit var postViewModel: PostViewModel
    private lateinit var postTitleEditText: EditText
    private lateinit var postContentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imageView: ImageView

    // Use Safe Args to get the postId
    private val args: EditPostFragmentArgs by navArgs()
    private var postId: String = args.postId

    private var originalImagePath: String? = null
    private var originalOwner: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                imageView.setImageURI(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseService = FirebaseService()
        val cloudinaryModel = CloudinaryModel()
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao, firebaseService)
        val postViewModelFactory = PostViewModelFactory(postRepository, cloudinaryModel)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)

        postTitleEditText = view.findViewById(R.id.et_post_title)
        postContentEditText = view.findViewById(R.id.et_post_content)
        saveButton = view.findViewById(R.id.btn_save_post)
        selectImageButton = view.findViewById(R.id.btn_select_image)
        imageView = view.findViewById(R.id.iv_post_image)

        // Load the existing post details
        postViewModel.getPostById(postId)
        postViewModel.post.observe(viewLifecycleOwner) { post ->
            post?.let {
                postTitleEditText.setText(it.title)
                postContentEditText.setText(it.content)
                originalImagePath = it.imagePath // Save the original image path
                originalOwner = it.owner // Save the original owner
                imageUri = Uri.parse(it.imagePath) // Assuming imagePath is stored as a URI
                Glide.with(requireContext()).load(imageUri).into(imageView) // Display the current image
            } ?: run {
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Open gallery to select a new image
        selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val updatedTitle = postTitleEditText.text.toString().trim()
            val updatedContent = postContentEditText.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Title and content cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Use the existing image path if no new image is selected
            val finalImagePath = imageUri?.toString() ?: originalImagePath

            // Use the original owner if it hasn't changed
            val finalOwner = originalOwner ?: "" // If owner is not null, retain original owner

            // Create a Post object with updated data
            val updatedPost = Post(
                id = postId,  // Use existing post ID
                title = updatedTitle,
                content = updatedContent,
                imagePath = finalImagePath ?: "",
                owner = finalOwner // Retain the original owner
            )

            postViewModel.updatePost(updatedPost)

            // Observe the result of the update operation
            postViewModel.postUpdateResult.observe(viewLifecycleOwner) { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        "Post updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update post", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
