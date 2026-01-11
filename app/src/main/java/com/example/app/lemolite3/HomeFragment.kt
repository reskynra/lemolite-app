package com.example.app.lemolite3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.lemolite3.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private lateinit var postListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("posts")

        setupListView()
        fetchPosts()
    }

    private fun setupListView() {
        postAdapter = PostAdapter(
            requireContext(),
            postList,
            onItemClick = { post ->
                val intent = Intent(requireContext(), DetailPostActivity::class.java)
                intent.putExtra("POST_ID", post.postId)
                startActivity(intent)
            },
            onLikeClick = { post ->
                handleLikePost(post)
            }
        )

        binding.lvPosts.adapter = postAdapter
    }

    private fun fetchPosts() {
        binding.progressBar.visibility = View.VISIBLE

        postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                postList.clear()

                if (snapshot.exists()) {
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { postList.add(it) }
                    }
                    postList.reverse()
                    binding.tvEmptyState.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.VISIBLE
                }

                postAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return

                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        database.addValueEventListener(postListener)
    }

    private fun handleLikePost(post: Post) {
        val postId = post.postId ?: return
        val postRef = database.child(postId)

        postRef.child("likesCount").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentLikes = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentLikes + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && isAdded) {
                    Toast.makeText(requireContext(), "Liked!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        database.removeEventListener(postListener)
        _binding = null
    }
}