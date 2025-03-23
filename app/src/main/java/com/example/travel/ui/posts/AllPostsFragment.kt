package com.example.travel.ui.posts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel.R
import com.example.travel.adapter.PostAdapter
import com.example.travel.data.AppDatabase
import com.example.travel.data.CloudinaryModel
import com.example.travel.data.firebase.FirebaseService
import com.example.travel.repository.PostRepository
import com.example.travel.ui.posts.SinglePostFragment
import com.example.travel.viewmodel.PostViewModel
import com.example.travel.viewmodel.PostViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllPostsFragment : Fragment(R.layout.fragment_all_posts) {

    private lateinit var postViewModel: PostViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val firebaseService = FirebaseService()
        val cloudinaryModel = CloudinaryModel()
        val postDao = AppDatabase.getDatabase(requireContext()).postDao()
        val postRepository = PostRepository(postDao, firebaseService)
        val postViewModelFactory = PostViewModelFactory(postRepository, cloudinaryModel)
        postViewModel = ViewModelProvider(this, postViewModelFactory).get(PostViewModel::class.java)

        Log.d("SinglePostScreen", "Logged-in User ID: $currentUserId")
        recyclerView = view.findViewById(R.id.recyclerView)
        postAdapter = PostAdapter { postId ->
            navigateToSinglePost(postId)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postAdapter

        postViewModel.syncAllPosts { success ->
            if (!success) {
                Toast.makeText(requireContext(), "Failed to sync posts from Firebase", Toast.LENGTH_SHORT).show()
            }
        }
        postViewModel.postList.observe(viewLifecycleOwner) { posts ->
                if (posts.isNotEmpty()) {
                    postAdapter.submitList(posts)
                } else {
                    Toast.makeText(requireContext(), "No posts available", Toast.LENGTH_SHORT)
                        .show()
                }
            }

    }

    private fun navigateToSinglePost(postId: String) {
        val action = AllPostsFragmentDirections.actionAllPostsFragmentToSinglePostFragment(postId)
        findNavController().navigate(action)
    }
}
