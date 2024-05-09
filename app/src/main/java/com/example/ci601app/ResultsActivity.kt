package com.example.ci601app

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_completion_screen) // Make sure this matches your XML file name
        supportActionBar?.hide()

        // Find UI elements
        val congratulationsText = findViewById<TextView>(R.id.congratulations_text)
        val homeButton = findViewById<Button>(R.id.Home_Button)
        val scoreCircleText = findViewById<TextView>(R.id.circle_score)

        // Extract the score from the intent
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val scorePercentage = if (totalQuestions > 0) correctAnswers * 100 / totalQuestions else 0

        // Update UI elements with quiz results
        congratulationsText.text = if (scorePercentage >= 50) "Congratulations!" else "Better luck next time!"
        setScoreText(scoreCircleText, correctAnswers, totalQuestions)

        // Handle home button click
        homeButton.setOnClickListener {
            // Navigate back to the MainActivity
            finish()
        }
    }

    private fun setScoreText(textView: TextView, score: Int, total: Int) {
        val scoreColor = if (score * 100 / total < 50) Color.RED else Color.GREEN
        val scoreString = score.toString()
        val totalString = "/$total"

        // Create a SpannableString for the score and total, coloring only the score part
        val spannable = SpannableStringBuilder(scoreString).apply {
            // Apply color to the score
            setSpan(ForegroundColorSpan(scoreColor), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Apply larger text size to the score
            setSpan(RelativeSizeSpan(1.5f), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Append the total with normal size
        spannable.append(totalString)

        // Set the complete text to TextView
        textView.text = SpannableStringBuilder("").append(spannable)
    }
}