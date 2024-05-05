package com.example.ci601app


import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class QuizActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var questionIndicatorTextView: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var timerTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private var currentQuestionIndex = 0
    private var lastClickTime: Long = 0
    private var selectedOptionIndex: Int = -1
    private val selectedOptions = MutableList(4) { -1 }
    private var questionsList = mutableListOf<Question>() // A data class for storing question details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Restore state if there is a previous state saved
        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex", 0)
        }

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.action_bar_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val actionBarTitle: TextView = supportActionBar?.customView?.findViewById(R.id.action_bar_title) as TextView

        // Setting the ActionBar title dynamically from the Intent
        val quizName = intent.getStringExtra("QUIZ_NAME") ?: "Quiz"
        actionBarTitle.text = "$quizName Quiz"

        questionIndicatorTextView = findViewById(R.id.question_indicator_textview)
        setupUI()
        updateBackButtonState()
        loadRandomQuestionsFromFirebase()

        val quizTime = intent.getLongExtra("QUIZ_TIME", 300000) // Default to 5 minutes if no value passed
        startTimer(quizTime) // Start timer with the specified duration
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current question index
        outState.putInt("currentQuestionIndex", currentQuestionIndex)
    }

    private fun updateBackButtonState() {
        // Enable back button if currentQuestionIndex is greater than 0
        // Disable it otherwise
        val backButtonEnabled = currentQuestionIndex > 0
        backButton.isEnabled = backButtonEnabled
        Log.d("QuizActivity", "Back button enabled state: $backButtonEnabled")
    }

    private fun updateQuestionIndicator() {
        val totalQuestions = questionsList.size
        val currentQuestionNumber = currentQuestionIndex + 1
        val indicatorText = "Question $currentQuestionNumber/$totalQuestions"
        questionIndicatorTextView.text = indicatorText
    }
    private fun setupUI() {
        questionTextView = findViewById(R.id.question_textview)
        timerTextView = findViewById(R.id.timer_indicator_textview)
        progressIndicator = findViewById(R.id.question_progress_indicator)
        backButton = findViewById(R.id.btn_back)
        nextButton = findViewById(R.id.btn_next)

        backButton.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE
        backButton.isEnabled = currentQuestionIndex > 0

        val answers = arrayOf(
            findViewById<Button>(R.id.btn0),
            findViewById<Button>(R.id.btn1),
            findViewById<Button>(R.id.btn2),
            findViewById<Button>(R.id.btn3)
        )

        // Set click listeners for answer buttons
        answers.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (currentQuestionIndex in questionsList.indices) {
                    if (selectedOptions[currentQuestionIndex] == -1) {  // Ensure no previous selection for this question
                        selectedOptions[currentQuestionIndex] = index
                        highlightAnswers(answers, questionsList[currentQuestionIndex].correctAnswerIndex, index)
                        answers.forEach { it.isEnabled = false }  // Disable all answers to prevent changes
                        Log.d("QuizActivity", "Answer selected at index: $index for question: $currentQuestionIndex")
                    }
                } else {
                    Log.e("QuizActivity", "Attempt to access invalid index: $currentQuestionIndex")
                }
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestionIndex < questionsList.size - 1) {
                currentQuestionIndex++
                resetAnswers(answers)
                setQuestion(currentQuestionIndex)
            } else {
                finishQuiz()
            }
        }

        backButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                resetAnswers(answers)
                setQuestion(currentQuestionIndex)
            }
            backButton.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE
            backButton.isEnabled = currentQuestionIndex > 0
        }

        if (questionsList.isNotEmpty()) {
            setQuestion(currentQuestionIndex)  // Initialize with the first or current question
        }
    }

    private fun resetAnswers(answers: Array<Button>) {
        answers.forEach { button ->
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.quizoptions_border)
        }
    }
    private fun loadRandomQuestionsFromFirebase() {
        val category = intent.getStringExtra("QUIZ_CATEGORY") ?: return
        database = FirebaseDatabase.getInstance().getReference("quizzes/$category")

        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val allQuestions = mutableListOf<Question>()
                snapshot.children.forEach { dataSnapshot ->
                    val question = dataSnapshot.child("question").getValue(String::class.java) ?: "Default Question"
                    val correctAnswer = dataSnapshot.child("answer").getValue(Int::class.java) ?: -1
                    val options = dataSnapshot.child("options").children.map { option ->
                        option.getValue(String::class.java) ?: "Default Option"
                    }
                    allQuestions.add(Question(question, options, correctAnswer))
                }

                allQuestions.shuffle()
                val selectedQuestions = allQuestions.take(10)

                questionsList.clear()
                questionsList.addAll(selectedQuestions)
                selectedOptions.clear()
                selectedOptions.addAll(List(selectedQuestions.size) { -1 }) // Reset with -1 indicating no selection

                runOnUiThread {
                    if (questionsList.isNotEmpty()) {
                        setQuestion(currentQuestionIndex)  // Load the first question
                    }
                }
            }
        }
    }

    //original code that works properly
    /* private fun setQuestion(index: Int) {
         if (index in 0 until questionsList.size) {
             val question = questionsList[index]
             questionTextView.text = question.questionText
             question.options.forEachIndexed { i, option ->
                 val buttonId = when (i) {
                     0 -> R.id.btn0
                     1 -> R.id.btn1
                     2 -> R.id.btn2
                     else -> R.id.btn3
                 }

                 val button = findViewById<Button>(buttonId)
                 button.text = option
             }
             val progress = (index + 1) * 100 / questionsList.size
             progressIndicator.setProgressCompat(progress, true)
             updateQuestionIndicator()
         }
     }
     */

    private fun setQuestion(index: Int) {
        if (index in questionsList.indices) {
            val question = questionsList[index]
            questionTextView.text = question.questionText

            val answers = arrayOf(
                findViewById<Button>(R.id.btn0),
                findViewById<Button>(R.id.btn1),
                findViewById<Button>(R.id.btn2),
                findViewById<Button>(R.id.btn3)
            )

            // Reset and setup answers
            answers.forEachIndexed { i, button ->
                // Reset to default state
                button.isEnabled = true
                button.visibility = if (i < question.options.size) View.VISIBLE else View.GONE
                button.text = question.options.getOrElse(i) { "" }
                button.setBackgroundResource(R.drawable.quizoptions_border)  // Default background

                // Remove any tick or cross drawable
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }

            updateProgressIndicator(index)
        } else {
            Log.e("QuizActivity", "Index out of bounds while setting question: $index")
        }
    }
    private fun updateProgressIndicator(currentIndex: Int) {
        val progress = (currentIndex + 1) * 100 / questionsList.size
        progressIndicator.setProgressCompat(progress, true)
        val questionIndicatorText = "Question ${currentIndex + 1}/${questionsList.size}"
        questionIndicatorTextView.text = questionIndicatorText
    }





    private fun checkAnswer(selectedIndex: Int, isCorrect: Boolean) {
        val correctAnswerIndex = questionsList[currentQuestionIndex].correctAnswerIndex
        if (isCorrect) {
            Toast.makeText(this, "Correct! Great job.", Toast.LENGTH_SHORT).show()
        } else {
            val correctOption = questionsList[currentQuestionIndex].options[correctAnswerIndex]
            Toast.makeText(this, "Incorrect. The correct answer was: $correctOption", Toast.LENGTH_LONG).show()
        }
    }


    private fun disableQuestionButtons() {
        val answers = arrayOf(
            findViewById<Button>(R.id.btn0),
            findViewById<Button>(R.id.btn1),
            findViewById<Button>(R.id.btn2),
            findViewById<Button>(R.id.btn3)
        )
        answers.forEach { it.isEnabled = false }  // Disable all answer buttons
    }

    private fun highlightAnswers(answers: Array<Button>, correctIndex: Int, selectedIndex: Int) {
        answers.forEachIndexed { index, button ->
            when (index) {
                correctIndex -> button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_tick_24, 0)
                selectedIndex -> {
                    if (selectedIndex != correctIndex) {
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.wrong_answer_24, 0)
                    }
                }
                else -> button.setBackgroundResource(R.drawable.quizoptions_border)
            }
        }
    }
    private fun startTimer(timeInMillis: Long) {
        val animator = ValueAnimator.ofInt(100, 0).apply {
            duration = timeInMillis
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Int
                progressIndicator.setProgressCompat(progress, true)
            }
            doOnEnd {
                timerTextView.text = "Time's Up!"
                finishQuiz()
                progressIndicator.progress = 0
            }
        }
        animator.start()

        object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerTextView.text = String.format("%d:%02d", minutes, seconds)
            }
            override fun onFinish() { /* Timer finish is handled by animator */ }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastClickTime < 1000) {
            return  // Ignore the press.
        }
        lastClickTime = System.currentTimeMillis()
        super.onBackPressed()
    }




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


    data class Question(
        val questionText: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )
}