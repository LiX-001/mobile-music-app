package com.example.musicapp

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.net.Uri
import android.widget.Toast
import android.os.Environment
import java.io.File
import android.widget.LinearLayout
import android.widget.SearchView
import android.media.MediaPlayer
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.database.Cursor
import kotlin.collections.MutableList


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
import android.view.VelocityTracker

import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.text.Editable
import android.text.TextWatcher


class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var duration: TextView
    private lateinit var currentTime: TextView
    private lateinit var previousBtn: ImageButton
    private lateinit var nextBtn: ImageButton

    private lateinit var SongTitle: TextView
    private lateinit var ArtistName: TextView  
    private lateinit var AlbumArt: ImageView

    private lateinit var playerLayout: LinearLayout
    private var startY = 0f
    private var isMiniPlayer = false

    private lateinit var songList: RecyclerView
    private lateinit var queueRecyclerView: RecyclerView
    private lateinit var audioAdapter: AudioAdapter
    private val audioList = mutableListOf<AudioFile>()
    private lateinit var queueLayout: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val seekBarHandler = Handler(Looper.getMainLooper())

    private var browsingSource: Int = 0     // 0 = Local Storage ; 1 = Online Sources
    private var currentlyPlayingAudio: AudioFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            // Request necessary permissions
            requestAudioPermission()

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

            playerLayout = findViewById(R.id.playerLayout)

            SongTitle = findViewById<TextView>(R.id.SongTitle)
            ArtistName = findViewById<TextView>(R.id.ArtistName)
            AlbumArt = findViewById<ImageView>(R.id.AlbumArt)

            songList = findViewById(R.id.audioList)
            songList.layoutManager = LinearLayoutManager(this)
            
            queueLayout = findViewById<LinearLayout>(R.id.queueLayout)
            queueRecyclerView = findViewById(R.id.queueRecyclerView)
            queueRecyclerView.layoutManager = LinearLayoutManager(this)

            // Startup actions
            collapseToMiniPlayer()
            SongTitle.isSelected = true
            ArtistName.isSelected = true

            val queueAdapter = QueueAdapter(queue) { audio -> playAudio(audio) }
            
            // Initialize queue RecyclerView
            queueRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = queueAdapter
            }
            
            // Initialize main song list adapter
            audioAdapter = AudioAdapter(audioList,
                onItemClick = { audio: AudioFile ->
                    playAudio(audio) // Play song when clicked
                },
                onAddClick = { audio: AudioFile ->
                    queue.add(audio)
                    queueAdapter.notifyDataSetChanged() // Update queue RecyclerView
                    Toast.makeText(this, "${audio.title} was added to queue.", Toast.LENGTH_SHORT).show()
                }
            )
            
            songList.adapter = audioAdapter
            audioAdapter.notifyDataSetChanged()
            
            val queueButton: Button = findViewById(R.id.queueButton)
            queueButton.setOnClickListener {
                if (queueLayout.visibility == View.VISIBLE) {
                    queueLayout.visibility = View.GONE
                    songList.visibility = View.VISIBLE
                } else {
                    queueLayout.visibility = View.VISIBLE
                    collapseToMiniPlayer()
                    songList.visibility = View.GONE
                }
            }



            // Previous and Next buttons
            previousBtn.setOnClickListener {
                val currentIndex = queue.indexOfFirst { it.id == currentlyPlayingAudio?.id }
                if (currentIndex > 0) {
                    val previousAudio = queue[currentIndex - 1]
                    playAudio(previousAudio)
                } else {
                    Toast.makeText(this, "No previous song in queue.", Toast.LENGTH_SHORT).show()
                }
            }

            nextBtn.setOnClickListener {
                val currentIndex = queue.indexOfFirst { it.id == currentlyPlayingAudio?.id }
                if (currentIndex < queue.size - 1) {
                    val nextAudio = queue[currentIndex + 1]
                    playAudio(nextAudio)
                } else {
                    Toast.makeText(this, "No next song in queue.", Toast.LENGTH_SHORT).show()
                }
            }

            
            

            // Check and request storage permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                fetchAudioFiles()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
            }


            // Search Feature
            val searchIcon: ImageButton = findViewById(R.id.search_icon)
            val searchView: SearchView = findViewById(R.id.searchView)
            searchIcon.setOnClickListener {
                Toast.makeText(this, "Search Clicked!", Toast.LENGTH_SHORT).show()
                if (searchView.visibility == View.VISIBLE) {
                    searchView.visibility = View.GONE
                } else {
                    searchView.visibility = View.VISIBLE
                }
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    audioAdapter.filter(newText ?: "")
                    return true
                }
            })

            

            // Touch listener for swiping into MiniPlayer
            playerLayout.setOnTouchListener(object : View.OnTouchListener {
                private var startY = 0f
                private var velocityTracker: VelocityTracker? = null

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startY = event.rawY
                            velocityTracker = VelocityTracker.obtain()
                            velocityTracker?.addMovement(event)
                            return true
                        }
                
                        MotionEvent.ACTION_MOVE -> {
                            velocityTracker?.addMovement(event)
                            val deltaY = event.rawY - startY
                            playerLayout.translationY += deltaY
                            startY = event.rawY
                            return true
                        }
                
                        MotionEvent.ACTION_UP -> {
                            velocityTracker?.computeCurrentVelocity(1000)
                            val velocityY = velocityTracker?.yVelocity ?: 0f
                            val halfwayPoint = playerLayout.height / 3  // Midpoint
                
                            velocityTracker?.recycle()
                            velocityTracker = null
                
                            // Expand if dragged up past halfway or swiped up fast
                            if (playerLayout.translationY < halfwayPoint || velocityY < -100) {  
                                expandToFullPlayer()
                            } else {  
                                collapseToMiniPlayer()
                            }
                            return true
                        }
                    }
                    return false
                }
            })



            // Play/Pause button
            playPauseButton.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    handler.removeCallbacksAndMessages(null)
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
    var queue = mutableListOf<AudioFile>()
    // Play audio file
    private fun playAudio(audio: AudioFile) {
        currentlyPlayingAudio = audio
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        mediaPlayer = MediaPlayer()
    
        val audioUri = Uri.parse(audio.filePath)
    
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MainActivity, audioUri)
            setOnPreparedListener {
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                SongTitle.text = audio.title
                ArtistName.text = audio.artist
                mediaPlayer.start()
                updateSeekBar()
                updateTime()
            }
            setOnCompletionListener {
                // Play next song in queue
                val currentIndex = queue.indexOfFirst { it.id == audio.id }
                // If the current index is last in the queue, stop playback
                if (currentIndex == queue.size - 1) {
                    mediaPlayer.stop()
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                    handler.removeCallbacksAndMessages(null)
                    seekBar.progress = 0
                    currentTime.text = "00:00"
                    return@setOnCompletionListener
                } else {
                    // Otherwise, play the next song
                    val nextAudio = queue[currentIndex + 1]
                    playAudio(nextAudio)
                }
                
            }
            setOnSeekCompleteListener {
                updateSeekBar() // Restart updates after seeking
            }
            prepareAsync()
        }
    }

    // Updates SeekBar
    private fun updateSeekBar() {
        seekBar.max = mediaPlayer.duration
    
        seekBarHandler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    seekBarHandler.postDelayed(this, 500) // Update every 0.5 seconds
                }
            }
        })
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
        animatePlayer(expand = false)
        playPauseButton.setBackgroundResource(0) 
    }

    private fun expandToFullPlayer() {
        animatePlayer(expand = true)
        playPauseButton.setBackgroundResource(R.drawable.rounded_button)
    }

    private fun fetchAudioFiles() {
        if (browsingSource == 0) { 
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Albums.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?"
            val selectionArgs = arrayOf("30000")

            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} ASC"
            audioList.clear() // Clear previous data to avoid duplication

            applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn) ?: "Unknown"
                    val duration = cursor.getLong(durationColumn)
                    val filePath = cursor.getString(dataColumn)
                    val albumId = cursor.getLong(albumIdColumn)

                    val thumbnail = Uri.withAppendedPath(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId.toString()
                    ).toString()


                    audioList.add(AudioFile(id, title, artist, duration, filePath, thumbnail))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun requestAudioPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 101)
            } else {
                fetchAudioFiles()
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10-12 (API 29-32)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            } else {
                fetchAudioFiles()
            }
        } else {
            // Android 9 and below (API < 29)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    101
                )
            } else {
                fetchAudioFiles()
            }
        }
    }


    private fun animatePlayer(expand: Boolean) {
        val params = playerLayout.layoutParams
        val albumArt = findViewById<ImageView>(R.id.AlbumArt)
        val albumArtLayout = findViewById<LinearLayout>(R.id.albumArtLayout)
        val albumArtParams = albumArt.layoutParams

        // Define target values based on state (expand or collapse)
        val targetHeight = if (expand) ViewGroup.LayoutParams.MATCH_PARENT else (150 * resources.displayMetrics.density).toInt()
        val targetTranslationY = if (expand) 0f else (500f * resources.displayMetrics.density)
        val targetCoverSize = if (expand) (300 * resources.displayMetrics.density).toInt() else 150
        val targetTextSizeSong = if (expand) 25f else 14f
        val targetTextSizeArtist = if (expand) 15f else 10f
        val visibility = if (expand) View.VISIBLE else View.GONE
        val listvisibility = if (expand) View.GONE else View.VISIBLE
        val gravity = if (expand) Gravity.CENTER else Gravity.START
        val orientation = if (expand) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL

        // Changing Play/Pause Button Style
        if (expand) playPauseButton.setBackgroundResource(0) else playPauseButton.setBackgroundResource(R.drawable.rounded_button)

        // Height animation
        val playerHeight = ValueAnimator.ofInt(playerLayout.height, targetHeight).apply {
            duration = 300
            addUpdateListener { animation ->
                params.height = animation.animatedValue as Int
                playerLayout.layoutParams = params
            }
        }

        // Width & height animation for album art
        val coverWidth = ValueAnimator.ofInt(albumArt.width, targetCoverSize).apply {
            duration = 300
            addUpdateListener { animation ->
                albumArtParams.width = animation.animatedValue as Int
                albumArt.layoutParams = albumArtParams
            }
        }

        val coverHeight = ValueAnimator.ofInt(albumArt.height, targetCoverSize).apply {
            duration = 300
            addUpdateListener { animation ->
                albumArtParams.height = animation.animatedValue as Int
                albumArt.layoutParams = albumArtParams
            }
        }

        // Player translation animation
        val playerLocation = ObjectAnimator.ofFloat(playerLayout, "translationY", targetTranslationY).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        // Fade out before layout changes
        val fadeOut = ObjectAnimator.ofFloat(playerLayout, "alpha", 1f, 0f).apply {
            duration = 150
        }

        // Fade in after layout changes
        val fadeIn = ObjectAnimator.ofFloat(playerLayout, "alpha", 0f, 1f).apply {
            duration = 150
            startDelay = 150 // Ensures it starts after fade-out completes
        }

        // Animate text size
        val textSizeSong = ValueAnimator.ofFloat(findViewById<TextView>(R.id.SongTitle).textSize, targetTextSizeSong).apply {
            duration = 200
            addUpdateListener { animator ->
                findViewById<TextView>(R.id.SongTitle).textSize = animator.animatedValue as Float
            }
        }

        val textSizeArtist = ValueAnimator.ofFloat(findViewById<TextView>(R.id.ArtistName).textSize, targetTextSizeArtist).apply {
            duration = 200
            addUpdateListener { animator ->
                findViewById<TextView>(R.id.ArtistName).textSize = animator.animatedValue as Float
            }
        }

        // Apply layout changes after fade-out completes
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Update layout after fade-out
                albumArtLayout.orientation = orientation
                albumArtLayout.layoutDirection = View.LAYOUT_DIRECTION_INHERIT
                albumArtLayout.gravity = gravity
                findViewById<LinearLayout>(R.id.songDetails).gravity = gravity

                // Update visibilities with fade effect
                currentTime.visibility = visibility
                duration.visibility = visibility
                if (queueLayout.visibility == View.VISIBLE) {
                    queueLayout.visibility = listvisibility
                    songList.visibility = visibility
                }
                songList.visibility = listvisibility
                findViewById<Button>(R.id.queueButton).visibility = visibility
            }
        })

        // Run all animations together
        AnimatorSet().apply {
            playTogether(playerLocation, playerHeight, coverWidth, coverHeight, textSizeSong, textSizeArtist, fadeOut, fadeIn)
            start()
        }

        isMiniPlayer = !expand // Toggle the state
    }

    // Some personal functions
    fun spToPx(sp: Float, context: Context): Float {
        return sp * (context.resources.displayMetrics.densityDpi / 160f)
    }

}
