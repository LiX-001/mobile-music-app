package com.example.musicapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            Log.d("DEBUG", "setContentView() is working.")
            Toast.makeText(this, "setContentView() is working.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.d("DEBUG", "error in setContentView() : ", e)  // Log when activity starts
            Toast.makeText(this, "error in setContentView() : "+e, Toast.LENGTH_LONG).show()
        }

        Log.d("DEBUG", "MainActivity started")  // Log when activity starts
        Toast.makeText(this, "App Started", Toast.LENGTH_LONG).show()
        
        // UI elements
        val textView: TextView = findViewById(R.id.textView1)
        val button: Button = findViewById(R.id.button1)

        // On button click 
        button.setOnClickListener {
            textView.text = "Build Successful!"
        }

    }
}