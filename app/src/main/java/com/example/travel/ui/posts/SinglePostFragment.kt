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
import com.example.travel.data.CloudinaryModel
import com.example.travel.data.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.travel.ui.posts.SinglePostFragmentArgs



class SinglePostFragment : Fragment(R.layout.post_fragment) {

    private lateinit var postViewModel: PostViewModel
    private lateinit var postTitleTextView: TextView
    private lateinit var postContentTextView: TextView
    private lateinit var postImageView: ImageView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var postAuthorTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseService = FirebaseService()
        val cloudinaryModel = CloudinaryModel()
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao,firebaseService)
        val postViewModelFactory = PostViewModelFactory(postRepository, cloudinaryModel)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)

        // Get the postId from arguments
        val args = SinglePostFragmentArgs.fromBundle(requireArguments())
        val postId = args.postId

        // Set up UI elements
        postTitleTextView = view.findViewById(R.id.tv_post_title)
        postContentTextView = view.findViewById(R.id.tv_post_content)
        postImageView = view.findViewById(R.id.iv_post_image)
        editButton = view.findViewById(R.id.btn_edit)
        deleteButton = view.findViewById(R.id.btn_delete)
        postAuthorTextView = view.findViewById(R.id.tv_post_author)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d("SinglePost","Current User is: $currentUserId")
        // Fetch the post by ID
        postViewModel.getPostById(postId)
        Log.d("SinglePost", "Post id is $postId")
        // Observe the post LiveData
        postViewModel.post.observe(viewLifecycleOwner) { post ->
            Log.d("SinglePostFragment", "Fetched post: $post")

            if (post != null) {
                // Display the post details
                postTitleTextView.text = post.title
                postContentTextView.text = post.content
                firebaseService.getUserById(post.owner) { user ->
                    if (user != null) {
                        postAuthorTextView.text = "By: ${user.username}"
                    } else {
                        postAuthorTextView.text = "By: Unknown User"
                    }
                }
                Glide.with(requireContext())  // Use Glide to load the image
                    .load(post.imagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(postImageView)

                // Show edit/delete buttons only if the current user is the post owner
                if (currentUserId.isNotEmpty() && currentUserId == post.owner) {
                    editButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE
                } else {
                    editButton.visibility = View.GONE
                    deleteButton.visibility = View.GONE
                }

                editButton.setOnClickListener {
                    val action = SinglePostFragmentDirections.actionSinglePostFragmentToEditPostFragment(postId)
                    findNavController().navigate(action)
                }

                deleteButton.setOnClickListener {
                    deletePost(postId)
                }
            } else {
                // Handle the case where the post is not found
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(postId: String) {
        postViewModel.deletePost(postId)
        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

}
