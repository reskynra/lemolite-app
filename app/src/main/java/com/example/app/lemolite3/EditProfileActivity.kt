package com.example.app.lemolite3

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().reference.child("users")

        if (userId == null) {
            finish()
            return
        }

        setupToolbar()
        loadCurrentUserData()
        setupAutoScrollInput()

        binding.btnPreviewImage.setOnClickListener {
            previewImage()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadCurrentUserData() {
        showLoading(true)

        database.child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    showLoading(false)

                    if (!snapshot.exists()) return

                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.etFullName.setText(it.fullName)
                        binding.etUsername.setText(it.username)
                        binding.etBio.setText(it.bio)
                        binding.etProfileImageUrl.setText(it.profileImageUrl)

                        Glide.with(this@EditProfileActivity)
                            .load(it.profileImageUrl)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(binding.ivEditProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Gagal memuat data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupAutoScrollInput() {

        val inputs = listOf(
            binding.etProfileImageUrl,
            binding.etFullName,
            binding.etUsername,
            binding.etBio
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

    private fun previewImage() {
        val url = binding.etProfileImageUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "Masukkan URL gambar", Toast.LENGTH_SHORT).show()
            return
        }

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_user_placeholder)
            .error(android.R.drawable.ic_dialog_alert)
            .into(binding.ivEditProfile)
    }

    private fun saveProfileChanges() {
        val fullName = binding.etFullName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val imageUrl = binding.etProfileImageUrl.text.toString().trim()

        if (fullName.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Nama dan Username wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val updates = hashMapOf<String, Any>(
            "fullName" to fullName,
            "username" to username,
            "bio" to bio,
            "profileImageUrl" to imageUrl
        )

        database.child(userId!!)
            .updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()

                NotificationHelper(this).notifyEditProfile()

                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Gagal menyimpan perubahan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun scrollToView(view: View) {
        binding.scrollView.postDelayed({
            binding.scrollView.smoothScrollTo(0, view.bottom)
        }, 200)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnSaveProfile.isEnabled = !isLoading
    }
}