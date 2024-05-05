package com.example.ci601app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Fundamentals Quiz"
        }
    }
