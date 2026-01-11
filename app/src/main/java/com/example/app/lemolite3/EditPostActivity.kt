package com.example.app.lemolite3

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.ActivityEditPostBinding
import com.google.firebase.database.*

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding
    private lateinit var database: DatabaseReference
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        postId = intent.getStringExtra("POST_ID")
        if (postId.isNullOrEmpty()) {
            Toast.makeText(this, "ID Postingan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference.child("posts")

        setupToolbar()
        loadExistingPostData()
        setupAutoScrollInput()

        binding.btnUpdatePost.setOnClickListener {
            updatePost()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadExistingPostData() {
        showLoading(true)

        database.child(postId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    showLoading(false)

                    if (!snapshot.exists()) {
                        Toast.makeText(
                            this@EditPostActivity,
                            "Postingan sudah tidak tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return
                    }

                    val post = snapshot.getValue(Post::class.java)
                    post?.let {
                        binding.etEditTitle.setText(it.title)
                        binding.etEditCaption.setText(it.caption)

                        Glide.with(this@EditPostActivity)
                            .load(it.imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivEditPostImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(
                        this@EditPostActivity,
                        "Gagal memuat data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupAutoScrollInput() {

        val inputs = listOf(
            binding.etEditTitle,
            binding.etEditCaption
        )

        for (editText in inputs) {
            editText.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    scrollToView(view)
                }
            }

            editText.setOnClickListener {
                scrollToView(it)
            }
        }
    }

    private fun scrollToView(view: View) {
        binding.scrollView.postDelayed({
            binding.scrollView.smoothScrollTo(0, view.bottom)
        }, 200)
    }

    private fun updatePost() {
        val title = binding.etEditTitle.text.toString().trim()
        val caption = binding.etEditCaption.text.toString().trim()

        if (title.isEmpty()) {
            binding.etEditTitle.error = "Judul tidak boleh kosong"
            return
        }

        showLoading(true)

        val updates = hashMapOf<String, Any>(
            "title" to title,
            "caption" to caption
        )

        database.child(postId!!)
            .updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Postingan berhasil diperbarui",
                    Toast.LENGTH_SHORT
                ).show()

                NotificationHelper(this).notifyEditPost()
                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Gagal memperbarui postingan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        binding.btnUpdatePost.isEnabled = !isLoading
    }
}