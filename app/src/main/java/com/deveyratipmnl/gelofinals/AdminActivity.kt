package com.deveyratipmnl.gelofinals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isSuperAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val addButton = findViewById<Button>(R.id.add_button)
        val viewButton = findViewById<Button>(R.id.view_button)
        val updateButton = findViewById<Button>(R.id.update_button)
        val deleteButton = findViewById<Button>(R.id.delete_button)
        val signOutButton = findViewById<Button>(R.id.sign_out_button)

        val username = intent.getStringExtra("username")

        // Check if the current user is a Super Admin
        firestore.collection("users").whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userType = document.getString("type")
                    if (userType == "Super Admin") {
                        isSuperAdmin = true
                    }
                }
            }

        addButton.setOnClickListener {
            showAddUserDialog()
        }

        viewButton.setOnClickListener {
            startActivity(Intent(this, ViewUsersActivity::class.java))
        }

        updateButton.setOnClickListener {
            showUpdateUserDialog()
        }

        deleteButton.setOnClickListener {
            showDeleteUserDialog()
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.username_edit_text)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.password_edit_text)
        val userTypeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.user_type_radio_group)

        // Disable the Admin and Super Admin radio buttons if not Super Admin
        if (!isSuperAdmin) {
            dialogView.findViewById<RadioButton>(R.id.radio_admin).isEnabled = false
            dialogView.findViewById<RadioButton>(R.id.radio_super_admin).isEnabled = false
        }

        AlertDialog.Builder(this)
            .setTitle("Add User")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val userType = when (userTypeRadioGroup.checkedRadioButtonId) {
                    R.id.radio_super_admin -> "Super Admin"
                    R.id.radio_admin -> "Admin"
                    R.id.radio_user -> "User"
                    else -> ""
                }

                if (username.isNotEmpty() && password.isNotEmpty() && userType.isNotEmpty()) {
                    val user = hashMapOf(
                        "username" to username,
                        "password" to password,
                        "type" to userType
                    )
                    firestore.collection("users").add(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showUpdateUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_user, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.username_edit_text)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password_edit_text)

        AlertDialog.Builder(this)
            .setTitle("Update User")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val username = usernameEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()

                if (username.isNotEmpty() && newPassword.isNotEmpty()) {
                    firestore.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            } else {
                                for (document in documents) {
                                    firestore.collection("users").document(document.id)
                                        .update("password", newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showDeleteUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_user, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.username_edit_text)

        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                val username = usernameEditText.text.toString()

                if (username.isNotEmpty()) {
                    firestore.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            } else {
                                for (document in documents) {
                                    val userType = document.getString("type")
                                    if (!isSuperAdmin && (userType == "Admin" || userType == "Super Admin")) {
                                        Toast.makeText(this, "Admins cannot delete other admins or super admins", Toast.LENGTH_SHORT).show()
                                    } else {
                                        firestore.collection("users").document(document.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
