package com.example.musicapp

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.os.Environment
import java.io.File
import android.app.AlertDialog

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var browsingLocationText: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: Button
    private lateinit var seekBar: SeekBar
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    // Using try and catch block to alert an error directly on phone.
        try {
            setContentView(R.layout.activity_main)
            
            // UI Elements
            // Toolbar
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            val browsingSource = findViewById<TextView>(R.id.browsing_source)
            setSupportActionBar(toolbar)
            playPauseButton = findViewById(R.id.PlayPauseButton)
            seekBar = findViewById(R.id.seekBar1)

            // Search icon click
            val searchIcon: ImageButton = findViewById(R.id.search_icon)
            searchIcon.setOnClickListener {
                Toast.makeText(this, "Search Clicked!", Toast.LENGTH_SHORT).show()
                // Implement search functionality here
            }

            // Browsing location
            browsingSource.setOnClickListener {
                val options = arrayOf("Internet", "Internal Storage", "MicroSD", "External Storage")
                MaterialAlertDialogBuilder(this)
                .setTitle("Select Source")
                .setItems(options) { _, which ->
                    browsingSource.text = options[which]
                }
                .show()
            }








            // Initialize MediaPlayer with a local raw resource
            mediaPlayer = MediaPlayer.create(this, R.raw.sparkle)

            playPauseButton.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    playPauseButton.text = "Play"
                } else {
                    mediaPlayer.start()
                    playPauseButton.text = "Pause"
                    updateSeekBar()
                }
            }

            // SeekBar change listener
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        


        } catch (e: Exception) {
            Log.e("DEBUG", "error in setContentView() : ", e)  // Log when activity starts
            
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            MaterialAlertDialogBuilder(this)
                .setTitle("App Crash")
                .setMessage(e.toString())
                .setPositiveButton("OK") {
                    dialog, _ -> dialog.dismiss()
                }
                .show()
        }

    }

    // Function to update browsing location.
    fun updateBrowsingLocation(location: String) {
        browsingLocationText.text = "Browsing from: $location"
    }

    // Function to update seekbar with progress
    private fun updateSeekBar() {
        seekBar.max = mediaPlayer.duration
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 500) // Update every 500ms
                }
            }
        }, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}