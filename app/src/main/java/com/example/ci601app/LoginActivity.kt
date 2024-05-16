package com.example.ci601app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ci601app.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.graphics.drawable.ColorDrawable
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    // Using view binding to access the layout's UI elements
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the theme for the login screen
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the login button click listener
        binding.loginButton.setOnClickListener {
            // Get email and password from input fields
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Try to sign in with email and password
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If login is successful, get the user's email
                        val userEmail = auth.currentUser?.email ?: "User"
                        val userName = userEmail.substringBefore('@')
                        // Start MainActivity and pass the user's name
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("USER_NAME", userName)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // If login fails, show a toast with the error message
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid password. Please try again."
                            is FirebaseAuthInvalidUserException -> "No account found with this email. Please sign up."
                            else -> "Login error: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Show a message if email or password is empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the "Forgot Password" click listener
        binding.forgotPassword.setOnClickListener {
            // Create an AlertDialog for password reset
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val emailInput = view.findViewById<EditText>(R.id.emailBox)

            builder.setView(view)
            val dialog = builder.create()

            // Set up reset and cancel buttons in the dialog
            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                resetPassword(emailInput)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            // Make the dialog background transparent
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0)) // Transparent color
            }
            dialog.show()
        }

        // Set up the "Sign Up" redirect click listener
        binding.signupRedirectText.setOnClickListener {
            // Navigate to the SignupActivity
            val signupIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupIntent)
        }
    }

    // Function to handle password reset email
    private fun resetPassword(email: EditText) {
        val emailText = email.text.toString()
        // Check if email field is empty
        if (emailText.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }
        // Check if the email format is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        // Send password reset email
        auth.sendPasswordResetEmail(emailText).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Check your email for reset instructions", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
