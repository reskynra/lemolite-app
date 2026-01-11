package com.example.app.lemolite3

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.FragmentCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setupImagePreview()

        binding.btnPost.setOnClickListener {
            uploadPost()
        }
        binding.etCaption.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.etCaption.bottom)
                }
            }
        }
        binding.etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.etCaption.bottom)
                }
            }
        }
    }

    private fun setupImagePreview() {
        binding.etImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this@CreatePostFragment)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.ivPreview)
                } else {
                    binding.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        })
    }

    private fun uploadPost() {
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val caption = binding.etCaption.text.toString().trim()

        if (imageUrl.isEmpty() || title.isEmpty() || caption.isEmpty()) {
            Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Sesi login berakhir", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val database = FirebaseDatabase.getInstance().getReference("posts")
        val postId = database.push().key ?: return

        val postData = Post(
            postId = postId,
            uid = currentUser.uid,
            username = currentUser.displayName ?: "User",
            title = title,
            caption = caption,
            imageUrl = imageUrl,
            likesCount = 0
        )

        database.child(postId).setValue(postData)
            .addOnSuccessListener {
                showLoading(false)

                if (!isAdded) return@addOnSuccessListener

                Toast.makeText(context, "Postingan berhasil diunggah!", Toast.LENGTH_SHORT).show()
                NotificationHelper(requireContext()).notifyNewPost()

                clearInputs()

                (activity as? MainActivity)?.let {
                    it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                        R.id.bottom_navigation
                    ).selectedItemId = R.id.nav_home
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Gagal mengunggah postingan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        binding.etImageUrl.text?.clear()
        binding.etTitle.text?.clear()
        binding.etCaption.text?.clear()
        binding.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnPost.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}