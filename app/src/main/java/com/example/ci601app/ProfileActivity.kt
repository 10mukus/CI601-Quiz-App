package com.example.ci601app
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth  // Move this outside of onCreate to make it a class member

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()


        auth = FirebaseAuth.getInstance()

        val emailTextView = findViewById<TextView>(R.id.email_address)
        val userNameTextView = findViewById<TextView>(R.id.profileUsername)
        val passwordResetButton = findViewById<Button>(R.id.changePasswordBtn)
        val homeButton = findViewById<Button>(R.id.Home_Button)



        val user = auth.currentUser
        emailTextView.text = user?.email ?: "No Email Found"

        // Extracting the username from the email
        val email = user?.email ?: ""
        val username = email.substringBefore('@')
        val capitalizedUserName = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        userNameTextView.text = capitalizedUserName

        val logoutButton = findViewById<Button>(R.id.LogoutBtn)
        logoutButton.setOnClickListener {
            logoutUser()  // Call the function to handle user logout
        }
        passwordResetButton.setOnClickListener {
            user?.email?.let { it ->
                sendPasswordResetEmail(it)
            }
        }

        // Handle home button click
        homeButton.setOnClickListener {
            // Navigate back to the MainActivity
            finish()
        }
    }

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
    private fun logoutUser() {
        // Log out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Redirect to login activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()  // End this activity
    }
}
