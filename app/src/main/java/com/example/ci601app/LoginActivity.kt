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

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener{
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        val userEmail = firebaseAuth.currentUser?.email ?: "User"
                        val userName = userEmail.substringBefore('@')
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("USER_NAME", userName)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // Handling exceptions with more user-friendly messages
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid password. Please try again."
                            is FirebaseAuthInvalidUserException -> "No account found with this email. Please sign up."
                            else -> "Login error: ${exception?.localizedMessage}"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this,"Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPassword.setOnClickListener{

            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot,null)
            val userEmail = view.findViewById<EditText>(R.id.emailBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener{
                compareEmail(userEmail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener{
                dialog.dismiss()
            }
            if (dialog.window != null) {
                val colorDrawable = ColorDrawable(0) // Transparent color
                dialog.window!!.setBackgroundDrawable(colorDrawable)
            }
            dialog.show()
        }
            binding.signupRedirectText.setOnClickListener {
                val signupIntent = Intent(this,SignupActivity::class.java)
                startActivity(signupIntent)
            }
    }

        //Outside onCreate
        private fun compareEmail (email: EditText){
            if(email.text.toString().isEmpty()){
                return
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
                return
            }
            firebaseAuth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    Toast.makeText(this,"Check your email",Toast.LENGTH_SHORT).show()
                }
            }
        }

}