package com.example.ci601app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation()
        setupCardClickListeners()

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        val capitalizedUserName =
            userName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val userNameTextView: TextView = findViewById(R.id.userNameTextView)
        userNameTextView.text = getString(R.string.welcome_message, capitalizedUserName)

    }

    private fun setupCardClickListeners() {
        val myCardView: CardView = findViewById(R.id.mycardview)
        myCardView.setOnClickListener {
            launchQuizActivity("Fundamentals")
        }

        val networkSecurityCard: CardView = findViewById(R.id.networksecuritycard)
        networkSecurityCard.setOnClickListener {
            launchQuizActivity("Network Security")
        }

        val ethicalHackingCard: CardView = findViewById(R.id.ethicalhackingcard)
        ethicalHackingCard.setOnClickListener {
            launchQuizActivity("Ethical Hacking")
        }

        val cryptographyCard: CardView = findViewById(R.id.cryptographycard)
        cryptographyCard.setOnClickListener {
            launchQuizActivity("Cryptography")
        }

        val cyberLawsCard: CardView = findViewById(R.id.cyberlawscard)
        cyberLawsCard.setOnClickListener {
            launchQuizActivity("Cyber Laws")
        }

        val malwareCard: CardView = findViewById(R.id.malwarecard)
        malwareCard.setOnClickListener {
            launchQuizActivity("Malware")
        }
    }

    private fun launchQuizActivity(category: String) {
        val timeInMillis = when (category) {
            "Network Security" -> 600000 // 10 minutes
            "Ethical Hacking" -> 900000 // 15 minutes
            "Cryptography" -> 1200000 // 20 minutes
            "Cyber Laws" -> 300000 // 5 minutes
            "Malware" -> 450000 // 7.5 minutes
            else -> 300000 // Default 5 minutes for others
        }

        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("QUIZ_CATEGORY", category)
            putExtra("QUIZ_TIME", timeInMillis) // Passing the time duration
            putExtra("QUIZ_NAME", category)
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }











}