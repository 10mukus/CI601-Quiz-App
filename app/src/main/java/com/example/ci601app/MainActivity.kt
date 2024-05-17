package com.example.ci601app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    // This function is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the theme for this activity
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)
        // Set the layout for this activity
        setContentView(R.layout.activity_main1)

        // Set up the bottom navigation menu
        setupBottomNavigation()

        // Set up click listeners for the card views
        setupCardClickListeners()

        // Get the username from the intent extras or default to "User"
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        // Capitalize the first letter of the username
        val capitalizedUserName = userName.capitalize()
        // Find the TextView for the username and set its text
        val userNameTextView: TextView = findViewById(R.id.userNameTextView)
        userNameTextView.text = "Welcome, $capitalizedUserName!"
    }

    // Set up click listeners for each card view
    private fun setupCardClickListeners() {
        val myCardView: CardView = findViewById(R.id.mycardview)
        myCardView.setOnClickListener {
            startQuizActivity("Fundamentals")
        }

        val networkSecurityCard: CardView = findViewById(R.id.networksecuritycard)
        networkSecurityCard.setOnClickListener {
            startQuizActivity("Network Security")
        }

        val ethicalHackingCard: CardView = findViewById(R.id.ethicalhackingcard)
        ethicalHackingCard.setOnClickListener {
            startQuizActivity("Ethical Hacking")
        }

        val cryptographyCard: CardView = findViewById(R.id.cryptographycard)
        cryptographyCard.setOnClickListener {
            startQuizActivity("Cryptography")
        }

        val cyberLawsCard: CardView = findViewById(R.id.cyberlawscard)
        cyberLawsCard.setOnClickListener {
            startQuizActivity("Cyberlaws")
        }

        val malwareCard: CardView = findViewById(R.id.malwarecard)
        malwareCard.setOnClickListener {
            startQuizActivity("Malware")
        }
    }

    // Start the QuizActivity with the specified category
    private fun startQuizActivity(category: String) {
        var timeInMillis: Long = 300000 // Default 5 minutes for each quiz (Testing purposes)
        when (category) {
            "Network Security" -> timeInMillis = 480000 // 8 minutes
            "Ethical Hacking" -> timeInMillis = 600000 // 10 minutes
            "Cryptography" -> timeInMillis = 600000 // 10 minutes
            "Cyberlaws" -> timeInMillis = 480000 // 8 minutes
            "Malware" -> timeInMillis = 480000 // 8 minutes
            "Fundamentals"-> timeInMillis = 300000 // 5 minutes
        }
        // Create an intent to start the QuizActivity and pass the quiz details
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("QUIZ_CATEGORY", category)
        intent.putExtra("QUIZ_TIME", timeInMillis) // Pass the time duration
        intent.putExtra("QUIZ_NAME", category)
        // Start the QuizActivity
        startActivity(intent)
    }

    // Set up the bottom navigation view and handle item selections
    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigate to the MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    // Navigate to the ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
