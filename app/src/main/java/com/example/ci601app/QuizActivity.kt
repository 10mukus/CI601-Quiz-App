package com.example.ci601app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// This activity handles the quiz interface and functionality
class QuizActivity : AppCompatActivity() {

    // UI elements and variables
    private lateinit var questionTextView: TextView
    private lateinit var questionIndicatorTextView: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var timerTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private var currentQuestionIndex = 0
    private var lastClickTime: Long = 0
    private val selectedOptions = MutableList(4) { -1 }
    private var questionsList = mutableListOf<Question>()

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Restore previous state if available
        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex", 0)
        }

        // Set up the custom ActionBar layout
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.action_bar_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set ActionBar title based on quiz name
        val actionBarTitle: TextView = supportActionBar?.customView?.findViewById(R.id.action_bar_title)!!
        val quizName = intent.getStringExtra("QUIZ_NAME") ?: "Quiz"
        actionBarTitle.text = "$quizName Quiz"

        // Initialize UI elements
        questionIndicatorTextView = findViewById(R.id.question_indicator_textview)
        questionTextView = findViewById(R.id.question_textview)
        timerTextView = findViewById(R.id.timer_indicator_textview)
        progressIndicator = findViewById(R.id.question_progress_indicator)
        backButton = findViewById(R.id.btn_back)
        nextButton = findViewById(R.id.btn_next)

        // Set up button visibility and state
        backButton.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE
        backButton.isEnabled = currentQuestionIndex > 0

        // Load questions and start the quiz timer
        loadQuestionsFromFirebase()
        val quizTime = intent.getLongExtra("QUIZ_TIME", 300000)
        startTimer(quizTime)

        // Set up button click listeners
        setupButtonListeners()
    }

    // Save the current state of the activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current question index
        outState.putInt("currentQuestionIndex", currentQuestionIndex)
    }

    // Update the state of the back button
    private fun updateBackButtonState() {
        // Enable or disable the back button based on the current question index
        backButton.isEnabled = currentQuestionIndex > 0
        Log.d("QuizActivity", "Back button enabled state: ${backButton.isEnabled}")
    }

    // Set up button click listeners
    private fun setupButtonListeners() {
        // Get answer buttons
        val answerButtons = arrayOf(
            findViewById<Button>(R.id.btn0),
            findViewById<Button>(R.id.btn1),
            findViewById<Button>(R.id.btn2),
            findViewById<Button>(R.id.btn3)
        )

        // Set click listeners for answer buttons
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (currentQuestionIndex in questionsList.indices) {
                    if (selectedOptions[currentQuestionIndex] == -1) {
                        // Save the selected answer index
                        selectedOptions[currentQuestionIndex] = index
                        // Highlight the selected answer and correct answer
                        highlightAnswers(answerButtons, questionsList[currentQuestionIndex].correctAnswerIndex, index)
                        // Disable all answer buttons to prevent changes
                        answerButtons.forEach { it.isEnabled = false }
                        Log.d("QuizActivity", "Answer selected at index: $index for question: $currentQuestionIndex")
                    }
                } else {
                    Log.e("QuizActivity", "Invalid index: $currentQuestionIndex")
                }
            }
        }

        // Set click listener for next button
        nextButton.setOnClickListener {
            if (currentQuestionIndex < questionsList.size - 1) {
                // Move to the next question
                currentQuestionIndex++
                resetAnswers(answerButtons)
                setQuestion(currentQuestionIndex)
                updateProgressIndicator()
            } else {
                // Finish the quiz if there are no more questions
                finishQuiz()
            }
        }

        // Set click listener for back button
        backButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                // Move to the previous question
                currentQuestionIndex--
                setQuestion(currentQuestionIndex)
                updateProgressIndicator()
            }
            // Update back button visibility and state
            backButton.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE
            backButton.isEnabled = currentQuestionIndex > 0
        }

        // Display the current question if available
        if (questionsList.isNotEmpty()) {
            setQuestion(currentQuestionIndex)
        }
    }

    // Reset answer buttons to their default state
    private fun resetAnswers(answerButtons: Array<Button>) {
        answerButtons.forEach { button ->
            // Enable each button and reset its background
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.buttons_background)
        }
    }

    // Load random questions from Firebase
    private fun loadQuestionsFromFirebase() {
        // Get the quiz category from the intent
        val category = intent.getStringExtra("QUIZ_CATEGORY") ?: return
        database = FirebaseDatabase.getInstance().getReference("quizzes/$category")

        // Fetch questions from Firebase
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val allQuestions = mutableListOf<Question>()
                snapshot.children.forEach { dataSnapshot ->
                    val questionText = dataSnapshot.child("question").getValue(String::class.java) ?: "Default Question"
                    val correctAnswer = dataSnapshot.child("answer").getValue(Int::class.java) ?: -1
                    val options = dataSnapshot.child("options").children.map { option ->
                        option.getValue(String::class.java) ?: "Default Option"
                    }
                    allQuestions.add(Question(questionText, options, correctAnswer))
                }

                // Shuffle and select a subset of questions
                allQuestions.shuffle()
                val selectedQuestions = allQuestions.take(10)

                questionsList.clear()
                questionsList.addAll(selectedQuestions)
                selectedOptions.clear()
                selectedOptions.addAll(List(selectedQuestions.size) { -1 })

                // Update the UI on the main thread
                runOnUiThread {
                    if (questionsList.isNotEmpty()) {
                        setQuestion(currentQuestionIndex)
                    }
                }
            }
        }
    }

    // Set the current question and update UI
    private fun setQuestion(index: Int) {
        if (index in questionsList.indices) {
            val question = questionsList[index]
            questionTextView.text = question.questionText

            val answerButtons = arrayOf(
                findViewById<Button>(R.id.btn0),
                findViewById<Button>(R.id.btn1),
                findViewById<Button>(R.id.btn2),
                findViewById<Button>(R.id.btn3)
            )

            // Reset buttons to default state
            answerButtons.forEach { button ->
                button.setBackgroundResource(R.drawable.buttons_background)
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }

            // Set up buttons based on current question
            answerButtons.forEachIndexed { i, button ->
                button.text = question.options.getOrElse(i) { "" }
                if (selectedOptions[index] == -1) {
                    button.isEnabled = true
                } else {
                    button.isEnabled = false
                    if (i == selectedOptions[index]) {
                        button.setBackgroundResource(if (i == question.correctAnswerIndex) R.drawable.quizoptions_correct else R.drawable.quizoptions_wrong)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (i == question.correctAnswerIndex) R.drawable.correct_tick_24 else R.drawable.wrong_answer_24, 0)
                    }
                    if (i == question.correctAnswerIndex) {
                        button.setBackgroundResource(R.drawable.quizoptions_correct)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_tick_24, 0)
                    }
                }
            }
            updateProgressIndicator()
            backButton.visibility = if (index > 0) View.VISIBLE else View.GONE
            backButton.isEnabled = index > 0
        } else {
            Log.e("QuizActivity", "Index out of bounds: $index")
        }
    }

    // Update the progress indicator based on current question index
    private fun updateProgressIndicator() {
        val totalQuestions = questionsList.size
        val currentQuestionNumber = currentQuestionIndex + 1
        val progress = (currentQuestionNumber.toFloat() / totalQuestions.toFloat()) * 100
        progressIndicator.setProgressCompat(progress.toInt(), true)
        questionIndicatorTextView.text = "Question $currentQuestionNumber/$totalQuestions"
    }

    // Highlight correct and selected answers
    private fun highlightAnswers(answerButtons: Array<Button>, correctIndex: Int, selectedIndex: Int) {
        answerButtons.forEachIndexed { index, button ->
            when (index) {
                correctIndex -> {
                    button.setBackgroundResource(R.drawable.quizoptions_correct)
                    button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_tick_24, 0)
                }
                selectedIndex -> {
                    if (selectedIndex != correctIndex) {
                        button.setBackgroundResource(R.drawable.quizoptions_wrong)
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.wrong_answer_24, 0)
                    }
                }
                else -> button.setBackgroundResource(R.drawable.buttons_background)
            }
        }
    }

    // Start a countdown timer for the quiz
    private fun startTimer(timeInMillis: Long) {
        // Create and start a countdown timer
        object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerTextView.text = String.format("%d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "Time's Up!"
                finishQuiz()
            }
        }.start()
    }

    // Handle action bar item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // Prevent multiple quick presses of the back button
    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastClickTime < 1000) {
            return
        }
        lastClickTime = System.currentTimeMillis()
        super.onBackPressed()
    }

    // Finish the quiz and navigate to the results screen
    private fun finishQuiz() {
        Log.d("QuizActivity", "Finishing quiz. Total Questions: ${questionsList.size}, Current Index: $currentQuestionIndex")
        val totalQuestions = questionsList.size
        val correctAnswers = selectedOptions.withIndex().count { (index, answer) ->
            index < questionsList.size && answer >= 0 && questionsList[index].correctAnswerIndex == answer
        }

        val intent = Intent(this, ResultsActivity::class.java).apply {
            putExtra("TOTAL_QUESTIONS", totalQuestions)
            putExtra("CORRECT_ANSWERS", correctAnswers)
        }
        startActivity(intent)
        finish()
    }

    // Data class to represent a quiz question
    data class Question(
        val questionText: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )
}
