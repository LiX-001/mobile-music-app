package com.example.musicapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.os.Environment
import java.io.File
import android.app.AlertDialog

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
            Log.e("DEBUG", "error in setContentView() : ", e)  // Log when activity starts
            
            // Save error to a file.
            val errorFile = File(getExternalFilesDir(null), "error_log.txt")
            errorFile.writeText(e.toString())
            
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            AlertDialog.Builder(this, R.style.AppCompat_Dialog)
                .setTitle("App Crash")
                .setMessage(e.toString())
                .setPositiveButton("OK", null)
                .show()
        }

        // UI elements
        val textView: TextView = findViewById(R.id.textView1)
        val button: Button = findViewById(R.id.button1)

        // On button click 
        button.setOnClickListener {
            textView.text = "Build Successful!"
        }

    }
}