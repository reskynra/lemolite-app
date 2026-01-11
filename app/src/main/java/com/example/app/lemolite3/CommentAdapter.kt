package com.example.app.lemolite3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val commentList: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        holder.binding.tvCommentUsername.text = comment.username
        holder.binding.tvCommentText.text = comment.text

        Glide.with(holder.itemView.context)
            .load(comment.profileImageUrl ?: "")
            .placeholder(R.drawable.ic_user_placeholder)
            .error(R.drawable.ic_user_placeholder)
            .circleCrop()
            .into(holder.binding.ivCommentUserProfile)

        holder.binding.tvCommentTime?.text =
            formatTime(comment.timestamp)
    }

    override fun getItemCount(): Int = commentList.size

    fun updateData(newList: List<Comment>) {
        commentList.clear()
        commentList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatTime(timeMillis: Long?): String {
        if (timeMillis == null) return ""
        val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }
}