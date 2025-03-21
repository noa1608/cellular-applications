package com.example.travel.ui.posts

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travel.R
import com.example.travel.viewmodel.PostViewModel
import androidx.navigation.fragment.findNavController


class EditPostFragment : Fragment(R.layout.fragment_edit_post) {

    private lateinit var postViewModel: PostViewModel
    private lateinit var postTitleEditText: EditText
    private lateinit var postContentEditText: EditText
    private lateinit var saveButton: Button

    private var postId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId = arguments?.getLong("postId") ?: -1

        postViewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)

        postTitleEditText = view.findViewById(R.id.et_post_title)
        postContentEditText = view.findViewById(R.id.et_post_content)
        saveButton = view.findViewById(R.id.btn_save_post)

        postViewModel.getPostById(postId)
        postViewModel.post.observe(viewLifecycleOwner) { post ->
            post?.let {
                postTitleEditText.setText(it.title)
                postContentEditText.setText(it.content)
            } ?: run {
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val updatedTitle = postTitleEditText.text.toString()
            val updatedContent = postContentEditText.text.toString()
            postViewModel.updatePost(postId, updatedTitle, updatedContent)
            findNavController().navigateUp()
        }
    }
}
