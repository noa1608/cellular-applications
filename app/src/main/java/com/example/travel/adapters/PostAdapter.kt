package com.example.travel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.data.Post

class PostAdapter(private val onPostClick: (Long) -> Unit) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private val postList = mutableListOf<Post>()

    fun submitList(posts: List<Post>) {
        postList.clear() // Clear previous data
        postList.addAll(posts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_fragment, parent, false) // Ensure correct layout file
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post, onPostClick)
    }

    override fun getItemCount(): Int = postList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_post_title)
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_post_content)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_post_image)

        fun bind(post: Post, onPostClick: (Long) -> Unit) {
            titleTextView.text = post.title
            contentTextView.text = post.content
            Glide.with(itemView.context)
                .load(post.imagePath)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView)

            itemView.setOnClickListener {
                onPostClick(post.id) // Navigate to full post
            }
        }
    }
}
