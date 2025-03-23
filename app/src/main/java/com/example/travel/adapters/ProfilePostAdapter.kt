package com.example.travel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel.R
import com.example.travel.data.Post

class ProfilePostAdapter(
    private val postList: List<Post>,
    private val onPostClickListener: OnPostClickListener
) : RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    interface OnPostClickListener {
        fun onPostClick(postId: String)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        val postImage: ImageView = itemView.findViewById(R.id.iv_post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.postTitle.text = post.title
        Glide.with(holder.itemView.context)
            .load(post.imagePath)
            .into(holder.postImage)

        holder.postImage.setOnClickListener {
            onPostClickListener.onPostClick(post.id)
        }
    }

    override fun getItemCount(): Int = postList.size
}
