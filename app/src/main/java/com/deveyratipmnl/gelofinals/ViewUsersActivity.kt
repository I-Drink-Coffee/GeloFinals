package com.deveyratipmnl.gelofinals

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewUsersActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_users)

        firestore = FirebaseFirestore.getInstance()
        usersListView = findViewById(R.id.users_list_view)

        fetchUsers()
    }

    private fun fetchUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = mutableListOf<String>()
                for (document in documents) {
                    val username = document.getString("username")
                    val type = document.getString("type")
                    if (username != null && type != null) {
                        users.add("$username ($type)")
                    }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
                usersListView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
            }
    }
}
