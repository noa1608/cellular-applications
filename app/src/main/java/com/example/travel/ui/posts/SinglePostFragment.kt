package com.example.travel.ui.posts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.example.travel.repository.PostRepository
import com.example.travel.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth

class SinglePostFragment : Fragment(R.layout.post_fragment) {

    private lateinit var postViewModel: PostViewModel
    private lateinit var postTitleTextView: TextView
    private lateinit var postContentTextView: TextView
    private lateinit var postImageView: ImageView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao)
        val postViewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)
        // Get the postId from arguments
        val postId = arguments?.getLong("postId") ?: 1  // Default to 1 if not found

        // Set up UI elements
        postTitleTextView = view.findViewById(R.id.tv_post_title)
        postContentTextView =  view.findViewById(R.id.tv_post_content)
        postImageView = view.findViewById(R.id.iv_post_image)
        editButton = view.findViewById(R.id.btn_edit)
        deleteButton = view.findViewById(R.id.btn_delete)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch the post by ID
        postViewModel.getPostById(postId)

        // Observe the post LiveData
        postViewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                // Display the post details
                postTitleTextView.text = post.title
                postContentTextView.text = post.content
                Glide.with(requireContext())  // Use Glide to load the image
                    .load(post.imagePath)      // The image URL or path
                    .placeholder(R.drawable.placeholder_image)  // Optional placeholder
                    .error(R.drawable.error_image)  // Optional error image
                    .into(postImageView)
                if ( currentUserId == post.owner) {
                    editButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE
                } else {
                    editButton.visibility = View.GONE
                    deleteButton.visibility = View.GONE
                }
                editButton.setOnClickListener {
                    val bundle = Bundle().apply { putLong("postId", postId) }
                    findNavController().navigate(R.id.action_singlePostFragment_to_editPostFragment, bundle)
                }

                deleteButton.setOnClickListener {
                    deletePost(postId)
                }            } else {
                // Handle the case where the post is not found (optional)
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun deletePost(postId: Long) {
        postViewModel.deletePost(postId)
        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
