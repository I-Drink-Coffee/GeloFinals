package com.deveyratipmnl.gelofinals

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log class
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firestore = FirebaseFirestore.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Log input values
            Log.d("LoginActivity", "Username entered: $username")
            Log.d("LoginActivity", "Password entered: $password")

            if (username.isNotEmpty() && password.isNotEmpty()) {
                firestore.collection("users")
                    .whereEqualTo("username", username)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            Log.e("LoginActivity", "No matching user found") // Log error
                        } else {
                            val user = documents.first()
                            val userType = user.getString("type")
                            val intent = when (userType) {
                                "Super Admin" -> Intent(this, AdminActivity::class.java)
                                "Admin" -> Intent(this, AdminActivity::class.java)
                                "User" -> Intent(this, UserActivity::class.java)
                                else -> null
                            }
                            intent?.let {
                                it.putExtra("username", username)
                                startActivity(it)
                                finish()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Failed to retrieve user documents") // Log failure
                    }
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                Log.w("LoginActivity", "Username or password field empty") // Log warning
            }
        }
    }
}
