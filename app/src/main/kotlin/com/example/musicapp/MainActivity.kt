package com.example.musicapp

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.os.Environment
import java.io.File
import android.app.AlertDialog
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.animation.ObjectAnimator
import android.widget.LinearLayout
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

    // Player view
    private lateinit var playerLayout: LinearLayout
    private var isExpanded = true
    private val animationDuration = 300L

    private lateinit var gestureDetector: GestureDetector


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

            // Player layout
            playerLayout = findViewById(R.id.playerLayout)

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



            // Guesture to swipe the player to the bottom (or from thr botton to view)
            gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 == null || e2 == null) return false

                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    val SWIPE_THRESHOLD = 100
                    val SWIPE_VELOCITY_THRESHOLD = 100

                    if (kotlin.math.abs(diffY) > kotlin.math.abs(diffX)) { // Vertical swipe
                        if (kotlin.math.abs(diffY) > SWIPE_THRESHOLD && kotlin.math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                collapsePlayer()  // Swipe down
                            } else {
                                expandPlayer()  // Swipe up
                            }
                            return true
                        }
                    }
                    return false
                }
            })





            playerLayout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> true
                    MotionEvent.ACTION_MOVE -> {
                        playerLayout.translationY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (playerLayout.translationY > playerLayout.height / 2) {
                            collapsePlayer()
                        } else {
                            expandPlayer()
                        }
                        true
                    }
                    else -> false
                }
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
                        updateTime()
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
        handler.removeCallbacksAndMessages(null)

        initial_min = (((mediaPlayer.currentPosition)/1000)/60)
        initial_sec = (((mediaPlayer.currentPosition)/1000)%60)
        min = (((mediaPlayer.duration)/1000)/60)
        sec = (((mediaPlayer.duration)/1000)%60)
        duration.text = String.format("%02d:%02d", min, sec)
        currentTime.text = String.format("%02d:%02d", initial_min, initial_sec)

        val updateRunnable = object : Runnable {
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
        }

    handler.postDelayed(updateRunnable, 1000)
    }


    private fun collapsePlayer() {
        if (isExpanded) {
            isExpanded = false
            ObjectAnimator.ofFloat(playerLayout, "translationY", playerLayout.height.toFloat()).apply {
                duration = animationDuration
                start()
            }
            seekBar.visibility = View.GONE
            currentTime.visibility = View.GONE
            duration.visibility = View.GONE
        }
    }

    private fun expandPlayer() {
        if (!isExpanded) {
            isExpanded = true
            ObjectAnimator.ofFloat(playerLayout, "translationY", 0f).apply {
                duration = animationDuration
                start()
            }
            seekBar.visibility = View.VISIBLE
            currentTime.visibility = View.VISIBLE
            duration.visibility = View.VISIBLE
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.let { gestureDetector.onTouchEvent(it) } ?: super.onTouchEvent(event)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
