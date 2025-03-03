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

    // All important view declarations
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private var initial_min: Int = 0
    private var initial_sec: Int = 0
    private var min: Int = 0
    private var sec: Int = 0
    private lateinit var duration: TextView
    private lateinit var currentTime: TextView
    private lateinit var previousBtn: ImageButton
    private lateinit var nextBtn: ImageButton

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    // Using try and catch block to alert an error directly on phone.
        try {
            setContentView(R.layout.activity_main)
            
            // UI Elements
            // Toolbar
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            // Media elements
            playPauseButton = findViewById<ImageButton>(R.id.PlayPauseButton)
            seekBar = findViewById(R.id.seekBar)
            previousBtn = findViewById(R.id.PreviousButton)
            nextBtn = findViewById(R.id.NextButton)
            currentTime = findViewById(R.id.CurrentTime)
            duration = findViewById(R.id.Duration)


            // Search icon click
            val searchIcon: ImageButton = findViewById(R.id.search_icon)
            searchIcon.setOnClickListener {
                Toast.makeText(this, "Search Clicked!", Toast.LENGTH_SHORT).show()
                // Implement search functionality here
            }







            // Initialize MediaPlayer
            mediaPlayer = MediaPlayer.create(this, R.raw.sparkle)

            

            // Play/Pause button
            playPauseButton.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    mediaPlayer.start()
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                    updateSeekBar()
                    updateTime()
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

    // Set timed duration of the track.
    fun updateTime() {
        initial_min = (((mediaPlayer.currentPosition)/1000)/60)
        initial_sec = (((mediaPlayer.currentPosition)/1000)%60)
        min = (((mediaPlayer.duration)/1000)/60)
        sec = (((mediaPlayer.duration)/1000)%60)
        duration.text = String.format("%02d:%02d", min, sec)
        currentTime.text = String.format("%02d:%02d", initial_min, initial_sec)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    if (initial_sec == 59) {
                        initial_min += 1
                        initial_sec = 0
                    } else {
                        initial_sec +=1
                    }
                    currentTime.text = String.format("%02d:%02d", initial_min, initial_sec)
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}