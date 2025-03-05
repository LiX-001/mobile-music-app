package com.example.musicapp

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.os.Environment
import java.io.File
import android.animation.ObjectAnimator
import android.widget.LinearLayout
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var duration: TextView
    private lateinit var currentTime: TextView
    private lateinit var previousBtn: ImageButton
    private lateinit var nextBtn: ImageButton
    private lateinit var playerLayout: LinearLayout


    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            // Toolbar setup
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            // UI Elements
            playerLayout = findViewById(R.id.playerLayout)
            playPauseButton = findViewById(R.id.PlayPauseButton)
            seekBar = findViewById(R.id.seekBar)
            previousBtn = findViewById(R.id.PreviousButton)
            nextBtn = findViewById(R.id.NextButton)
            currentTime = findViewById(R.id.CurrentTime)
            duration = findViewById(R.id.Duration)

            val searchIcon: ImageButton = findViewById(R.id.search_icon)
            searchIcon.setOnClickListener {
                Toast.makeText(this, "Search Clicked!", Toast.LENGTH_SHORT).show()
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

            // SeekBar listener
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                        updateTime()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

        } catch (e: Exception) {
            Log.e("DEBUG", "Error in setContentView()", e)
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            MaterialAlertDialogBuilder(this)
                .setTitle("App Crash")
                .setMessage(e.toString())
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    // Updates SeekBar
    private fun updateSeekBar() {
        seekBar.max = mediaPlayer.duration
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 500)
                }
            }
        }, 500)
    }

    // Updates Current Time Display
    fun updateTime() {
        handler.removeCallbacksAndMessages(null)

        val initialMin = (mediaPlayer.currentPosition / 1000) / 60
        val initialSec = (mediaPlayer.currentPosition / 1000) % 60
        val min = (mediaPlayer.duration / 1000) / 60
        val sec = (mediaPlayer.duration / 1000) % 60
        duration.text = String.format("%02d:%02d", min, sec)
        currentTime.text = String.format("%02d:%02d", initialMin, initialSec)

        val updateRunnable = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    currentTime.text = String.format(
                        "%02d:%02d", (mediaPlayer.currentPosition / 1000) / 60, (mediaPlayer.currentPosition / 1000) % 60
                    )
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.postDelayed(updateRunnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
