package com.example.musicapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnPause = findViewById<Button>(R.id.btnPause)
        val btnStop = findViewById<Button>(R.id.btnStop)

        mediaPlayer = MediaPlayer.create(this, R.raw.sample_music)

        btnPlay.setOnClickListener { mediaPlayer?.start() }
        btnPause.setOnClickListener { mediaPlayer?.pause() }
        btnStop.setOnClickListener { 
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
