package com.example.musicapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("DEBUG", "MainActivity started")  // ✅ Log when activity starts
        Toast.makeText(this, "App Started", Toast.LENGTH_LONG).show()
       
        setContentView(R.layout.activity_main)
        

        // UI elements
        val textView: TextView = findViewById(R.id.textView1)
        val button: Button = findViewById(R.id.button1)

        // On button click 
        button.setOnClickListener {
            textView.text = "Build Successful!"
        }

    }
}