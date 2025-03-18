package com.example.travel.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_user_posts)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = layoutManager

        auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email ?: ""

        val db = AppDatabase.getDatabase(requireContext())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(db.userDao()))
        )[UserViewModel::class.java]

        postViewModel = ViewModelProvider(
            this,
            PostViewModelFactory(PostRepository(db.postDao()))
        )[PostViewModel::class.java]

        val tvUsername = view.findViewById<TextView>(R.id.tv_username)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)
        val profileImage = view.findViewById<ShapeableImageView>(R.id.profile_image)


        userViewModel.viewModelScope.launch {
            val user = userViewModel.getUserByEmail(userEmail)
            user?.let {
                tvUsername.text = it.username
                tvEmail.text = it.email

                it.profilePictureUrl?.let { path ->
                    Glide.with(this@ProfileFragment)
                        .load(path)
                        .circleCrop()
                        .into(profileImage)
                }
                postViewModel.getAllPosts().observe(viewLifecycleOwner) { allPosts ->
                    val userPosts = allPosts.filter { post -> post.owner == userEmail }
                    recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                    recyclerView.adapter = ProfilePostAdapter(userPosts)
                }
            }
        }
    }
}
