package com.example.ci601app

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_completion_screen) // Make sure this matches your XML file name
        supportActionBar?.hide() // Hide the action bar for a cleaner look

        // Find the UI elements
        val congratulationsText = findViewById<TextView>(R.id.congratulations_text)
        val homeButton = findViewById<Button>(R.id.Home_Button)
        val scoreCircleText = findViewById<TextView>(R.id.circle_score)

        // Get the score details from the previous screen
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val scorePercentage = if (totalQuestions > 0) correctAnswers * 100 / totalQuestions else 0

        // Update the congratulations text based on the score
        if (scorePercentage >= 50) {
            congratulationsText.text = "Congratulations!"
        } else {
            congratulationsText.text = "Better luck next time!"
        }

        // Display the score in the circle
        setScoreText(scoreCircleText, correctAnswers, totalQuestions)

        // Set up the home button to go back to the main screen
        homeButton.setOnClickListener {
            finish() // Close this activity
        }
    }

    // This function sets the score text with colors and sizes
    private fun setScoreText(textView: TextView, score: Int, total: Int) {
        val scoreColor = if (score * 100 / total < 50) Color.RED else Color.GREEN
        val scoreString = score.toString()
        val totalString = "/$total"

        // Create a spannable string for fancy text
        val spannable = SpannableStringBuilder(scoreString).apply {
            // Change the color of the score
            setSpan(ForegroundColorSpan(scoreColor), 0, length, 0)
            // Make the score text larger
            setSpan(RelativeSizeSpan(1.5f), 0, length, 0)
        }

        // Append the total questions text normally
        spannable.append(totalString)

        // Set the spannable text to the TextView
        textView.text = spannable
    }
}
