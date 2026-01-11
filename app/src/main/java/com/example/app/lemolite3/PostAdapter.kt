package com.example.app.lemolite3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.ItemPostBinding

class PostAdapter(
    context: Context,
    private val postList: List<Post>,
    private val onItemClick: (Post) -> Unit,
    private val onLikeClick: (Post) -> Unit
) : ArrayAdapter<Post>(context, 0, postList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemPostBinding
        val view: View

        if (convertView == null) {
            binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemPostBinding
            view = convertView
        }

        val post = getItem(position) ?: return view

        binding.tvPostUsername.text = post.username
        binding.tvPostTitle.text = post.title
        binding.tvPostLikeCount.text = "${post.likesCount} likes"

        Glide.with(context)
            .load(post.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.ivPostImage)

        Glide.with(context)
            .load(R.drawable.ic_user_placeholder)
            .circleCrop()
            .into(binding.ivProfilePicture)

        binding.root.setOnClickListener {
            onItemClick(post)
        }

        binding.btnLike.setOnClickListener {
            onLikeClick(post)
        }

        return view
    }
}