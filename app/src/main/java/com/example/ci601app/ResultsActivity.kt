package com.example.ci601app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.progressindicator.CircularProgressIndicator

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_completion_screen) // Make sure this matches your XML file name
        supportActionBar?.hide()

        // Find UI elements
        val congratulationsText = findViewById<TextView>(R.id.congratulations_text)
        val scoreText = findViewById<TextView>(R.id.score_text)
        val homeButton = findViewById<Button>(R.id.Home_Button)

        // Extract the score from the intent
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val scorePercentage = if (totalQuestions > 0) correctAnswers * 100 / totalQuestions else 0

        // Update UI elements with quiz results
        congratulationsText.text = if (scorePercentage >= 50) "Congratulations!" else "Better luck next time!"
        scoreText.text = "Your Score: $correctAnswers/$totalQuestions"

        // Handle home button click
        homeButton.setOnClickListener {
            // Navigate back to the MainActivity
            finish()
        }
    }
}


