package com.example.ci601app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        // Hide the action bar for a cleaner look
        supportActionBar?.hide()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to the UI elements
        val emailTextView = findViewById<TextView>(R.id.email_address)
        val userNameTextView = findViewById<TextView>(R.id.profileUsername)
        val passwordResetButton = findViewById<Button>(R.id.changePasswordBtn)
        val homeButton = findViewById<Button>(R.id.Home_Button)

        // Get the current user
        val user = auth.currentUser
        // Set the email address or a default message if no email found
        emailTextView.text = user?.email ?: "No Email Found"

        // Extract and format the username from the email
        val email = user?.email ?: ""
        val username = email.substringBefore('@')
        val capitalizedUserName = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        userNameTextView.text = capitalizedUserName

        // Set up the logout button click listener
        val logoutButton = findViewById<Button>(R.id.LogoutBtn)
        logoutButton.setOnClickListener {
            // Call the function to handle user logout
            logoutUser()
        }

        // Set up the password reset button click listener
        passwordResetButton.setOnClickListener {
            // Send a password reset email if the user's email is available
            user?.email?.let { sendPasswordResetEmail(it) }
        }

        // Set up the home button click listener
        homeButton.setOnClickListener {
            // Navigate back to the MainActivity
            finish()
        }
    }

    // Function to send a password reset email
    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Function to handle user logout
    private fun logoutUser() {
        // Log out from Firebase
        FirebaseAuth.getInstance().signOut()
        // Redirect to the login activity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()  // End this activity
    }
}
