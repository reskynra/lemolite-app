package com.example.app.lemolite3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.ActivityDetailPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPostBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationHelper: NotificationHelper

    private var postId: String? = null
    private var isLiked = false

    private lateinit var postListener: ValueEventListener
    private lateinit var commentListener: ValueEventListener

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true

        postId = intent.getStringExtra("POST_ID")
        if (postId == null) {
            Toast.makeText(this, "Post tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        notificationHelper = NotificationHelper(this)

        setupToolbar()
        setupCommentSection()
        loadPostDetail()

        binding.btnDetailLike.setOnClickListener { toggleLike() }
        binding.btnPostComment.setOnClickListener { postComment() }
        binding.btnOptions.setOnClickListener { showPopupMenu(it) }
        binding.etComment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollToCommentInput()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadPostDetail() {
        val postRef = database.child("posts").child(postId!!)

        postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java) ?: return

                binding.tvDetailUsername.text = post.username
                binding.tvDetailTitle.text = post.title
                binding.tvDetailCaption.text = post.caption
                binding.tvDetailLikeCount.text = "${post.likesCount} suka"
                binding.rvComments.scrollToPosition(commentList.size - 1)

                Glide.with(this@DetailPostActivity)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivDetailImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DetailPostActivity,
                    "Gagal memuat postingan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        postRef.addValueEventListener(postListener)
    }

    private fun setupCommentSection() {
        commentAdapter = CommentAdapter(commentList)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        val commentRef = database.child("comments").child(postId!!)

        commentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    comment?.let { commentList.add(it) }
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        commentRef.addValueEventListener(commentListener)
    }

    private fun scrollToCommentInput() {
        binding.root.postDelayed({
            binding.scrollView?.smoothScrollTo(0, binding.etComment.bottom)
            binding.rvComments.scrollToPosition(commentList.size - 1)
        }, 200)
    }

    private fun postComment() {
        val text = binding.etComment.text.toString().trim()
        if (text.isEmpty()) {
            binding.etComment.error = "Komentar tidak boleh kosong"
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val commentRef = database.child("comments").child(postId!!).push()

        val comment = Comment(
            commentId = commentRef.key ?: "",
            username = auth.currentUser?.displayName ?: "User",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        commentRef.setValue(comment).addOnSuccessListener {
            binding.etComment.text?.clear()
            binding.etComment.requestFocus()
            binding.rvComments.post {
                binding.rvComments.scrollToPosition(commentList.size)
            }
            notificationHelper.notifyNewComment()
            Toast.makeText(this, "Komentar terkirim", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLike() {
        val postRef = database.child("posts").child(postId!!)

        postRef.child("likesCount").runTransaction(object : Transaction.Handler {
            override fun doTransaction(data: MutableData): Transaction.Result {
                var likes = data.getValue(Int::class.java) ?: 0
                likes = if (isLiked) likes - 1 else likes + 1
                if (likes < 0) likes = 0
                data.value = likes
                return Transaction.success(data)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed) {
                    isLiked = !isLiked
                    updateLikeUI(isLiked)

                    notificationHelper.notifyLikePost()
                }
            }
        })
    }

    private fun updateLikeUI(liked: Boolean) {
        if (liked) {
            binding.btnDetailLike.setImageResource(R.drawable.ic_heart_filled)
            binding.btnDetailLike.setColorFilter(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
        } else {
            binding.btnDetailLike.setImageResource(R.drawable.heart)
            binding.btnDetailLike.setColorFilter(
                ContextCompat.getColor(this, android.R.color.darker_gray)
            )
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.post_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {
                    startActivity(
                        Intent(this, EditPostActivity::class.java)
                            .putExtra("POST_ID", postId)
                    )
                    true
                }
                R.id.menu_delete -> {
                    showDeleteDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Postingan")
            .setMessage("Apakah kamu yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePost()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePost() {
        database.child("posts").child(postId!!).removeValue()
            .addOnSuccessListener {
                notificationHelper.notifyDeletePost()
                Toast.makeText(this, "Postingan berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus postingan", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        database.child("posts").child(postId!!).removeEventListener(postListener)
        database.child("comments").child(postId!!).removeEventListener(commentListener)
    }
}