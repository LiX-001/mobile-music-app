package com.example.musicapp

import kotlin.math.roundToInt

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import android.content.Context
import android.os.Environment
import java.io.File
import android.widget.LinearLayout
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity

import android.animation.ObjectAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.Gravity

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
    private var startY = 0f
    private var isMiniPlayer = false


    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            // Toolbar setup
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            // UI Elements
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

            playerLayout = findViewById(R.id.playerLayout)
            @Suppress("NOTHING_TO_OVERRIDE", "ACCIDENTAL_OVERRIDE")
            // Touch listener for swiping into MiniPlayer
            playerLayout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY // Store the starting position
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = event.rawY - startY
                        playerLayout.translationY += deltaY // Move the layout
                        startY = event.rawY // Update start position for smooth movement
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Snap to full or mini position based on final position
                        if (playerLayout.translationY > 200) {
                            collapseToMiniPlayer()
                        } else {
                            expandToFullPlayer()
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


    private fun collapseToMiniPlayer() {
        // initial declarations
        val params = playerLayout.layoutParams
        val albumArt = findViewById<ImageView>(R.id.AlbumArt)
        val albumArtLayout = findViewById<LinearLayout>(R.id.albumArtLayout)
        val albumArtParams = albumArt.layoutParams

        params.height = (150 * resources.displayMetrics.density).toInt()
        playerLayout.layoutParams = params

        ObjectAnimator.ofFloat(playerLayout, "translationY", 500f * resources.displayMetrics.density).apply {
                duration = 300
                interpolator = DecelerateInterpolator()
                start()
            }
            isMiniPlayer = true
            

        // Changing cover size
        albumArtParams.width = 150 // in px
        albumArtParams.height = 150 // in px
        albumArt.layoutParams = albumArtParams

        // Changing text size
        findViewById<TextView>(R.id.SongTitle).textSize = 14f
        findViewById<TextView>(R.id.ArtistName).textSize = 10f

        // Changing view settings
        albumArtLayout.orientation = LinearLayout.HORIZONTAL
        albumArtLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR
        albumArtLayout.gravity = Gravity.START

        // Changing visibilities
        currentTime.visibility = View.GONE
        duration.visibility = View.GONE
        
        playPauseButton.setBackgroundResource(0)
            
    }

    private fun expandToFullPlayer() {
        // initial declarations
        val params = playerLayout.layoutParams
        val albumArt = findViewById<ImageView>(R.id.AlbumArt)
        val albumArtLayout = findViewById<LinearLayout>(R.id.albumArtLayout)
        val albumArtParams = albumArt.layoutParams

        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        playerLayout.layoutParams = params

        val playerSize = ObjectAnimator.ofFloat(playerLayout, "translationY", 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        val fadeOut = ObjectAnimator.ofFloat(playerLayout, "alpha", 1f, 0f).apply {
            duration = 200
        }

        val fadeIn = ObjectAnimator.ofFloat(playerLayout, "alpha", 0f, 1f).apply {
            duration = 200
            startDelay = 200 // Start after fadeOut completes
        }

        // Animate text size change smoothly
        val textSizeSong = ValueAnimator.ofFloat(14f, 25f).apply {
            duration = 200
            addUpdateListener { animator ->
                findViewById<TextView>(R.id.SongTitle).textSize = (animator.animatedValue as Float)
            }
        }

        val textSizeArtist = ValueAnimator.ofFloat(10f, 15f).apply {
            duration = 200
            addUpdateListener { animator ->
                findViewById<TextView>(R.id.ArtistName).textSize = (animator.animatedValue as Float)
            }
        }

        // Apply layout changes after fade out completes
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Changing view settings
                albumArtLayout.orientation = LinearLayout.VERTICAL
                albumArtLayout.layoutDirection = View.LAYOUT_DIRECTION_INHERIT
                albumArtLayout.gravity = Gravity.CENTER

                // Changing visibility (with fade-in effect)
                currentTime.animate().alpha(1f).setDuration(200).start()
                duration.animate().alpha(1f).setDuration(200).start()
            }
        })

        // Run all animations together in sequence
        AnimatorSet().apply {
            playSequentially(fadeOut, textSizeSong, textSizeArtist, fadeIn, playerSize)
            start()
        }

        
        playPauseButton.setBackgroundResource(R.drawable.rounded_button)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    // Some personal functions
    fun spToPx(sp: Float, context: Context): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }

}
