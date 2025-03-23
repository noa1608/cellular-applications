package com.example.travel.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.data.Post
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val onPostClick: (String) -> Unit) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val postList = mutableListOf<Post>()
    private val userNameCache = mutableMapOf<String, String>() // Cache user names

    fun submitList(posts: List<Post>) {
        postList.clear()
        postList.addAll(posts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_posts, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post, onPostClick, userNameCache)
    }

    override fun getItemCount(): Int = postList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_post_title)
      //  private val contentTextView: TextView = itemView.findViewById(R.id.tv_post_content)
        private val imageView: ImageView = itemView.findViewById(R.id.postImage)
        private val authorTextView: TextView = itemView.findViewById(R.id.tv_post_author)
        init {
            // Check if views are null to debug the issue
            if (titleTextView == null || imageView == null || authorTextView == null) {
                Log.e("PostViewHolder", "One or more views are null")
            }
        }
        fun bind(post: Post, onPostClick: (String) -> Unit, userNameCache: MutableMap<String, String>) {
            titleTextView.text = post.title
           // contentTextView.text = post.content
            if (userNameCache.containsKey(post.owner)) {
                authorTextView.text = "By: ${userNameCache[post.owner]}"
            } else {
                // Fetch username from Firestore
                FirebaseFirestore.getInstance().collection("users")
                    .document(post.owner)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val username = document.getString("username") ?: "Unknown User"
                            userNameCache[post.owner] = username // Cache the username
                            authorTextView.text = "By: $username"
                        } else {
                            authorTextView.text = "By: Unknown User"
                        }
                    }
                    .addOnFailureListener {
                        authorTextView.text = "By: Unknown User"
                    }
            }
            Glide.with(itemView.context)
                .load(post.imagePath)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView)

            itemView.setOnClickListener {
                onPostClick(post.id)
            }
        }

    }
}
