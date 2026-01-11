package com.example.app.lemolite3

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.app.lemolite3.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var userListener: ValueEventListener? = null
    private var postListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val userId = auth.currentUser?.uid
        if (userId == null) {
            navigateToLogin()
            return
        }

        loadUserProfile(userId)
        countUserPosts(userId)

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserProfile(userId: String) {
        val userRef = database.child("users").child(userId)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                if (!snapshot.exists()) return

                val user = snapshot.getValue(User::class.java) ?: return

                binding.tvUsername.text = user.username ?: "username"
                binding.tvFullName.text = user.fullName ?: "Nama Lengkap"
                binding.tvBio.text = user.bio ?: "Belum ada bio"

                if (!user.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_user_placeholder)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userRef.addValueEventListener(userListener!!)
    }

    private fun countUserPosts(userId: String) {
        val postRef = database.child("posts")
            .orderByChild("uid")
            .equalTo(userId)

        postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                binding.tvPostCount.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        postRef.addValueEventListener(postListener!!)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
            .setPositiveButton("Ya") { _, _ ->
                auth.signOut()
                navigateToLogin()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val userId = auth.currentUser?.uid
        userId?.let {
            userListener?.let { l ->
                database.child("users").child(it).removeEventListener(l)
            }
            postListener?.let { l ->
                database.child("posts").removeEventListener(l)
            }
        }

        _binding = null
    }
}