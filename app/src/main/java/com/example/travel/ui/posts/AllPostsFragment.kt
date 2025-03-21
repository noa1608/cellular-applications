package com.example.travel.ui.posts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel.R
import com.example.travel.adapter.PostAdapter
import com.example.travel.data.AppDatabase
import com.example.travel.repository.PostRepository
import com.example.travel.ui.posts.SinglePostFragment
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class AllPostsFragment : Fragment(R.layout.fragment_all_posts) {

    private lateinit var postViewModel: PostViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Initialize ViewModel
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao)
        val postViewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)

        Log.d("SinglePostScreen", "Logged-in User ID: $currentUserId")
        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        postAdapter = PostAdapter { postId ->
            navigateToSinglePost(postId)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postAdapter

        // Observe LiveData for posts
        postViewModel.postList.observe(viewLifecycleOwner) { posts ->
            if (posts.isNotEmpty()) {
                postAdapter.submitList(posts)
            } else {
                Toast.makeText(requireContext(), "No posts available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToSinglePost(postId: Long) {
        val bundle = Bundle().apply {
            putLong("postId", postId)
        }

        // Use NavController to navigate
        findNavController().navigate(R.id.action_allPostsFragment_to_singlePostFragment, bundle)
    }
}
