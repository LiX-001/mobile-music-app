package com.example.musicapp
// Can't bother specific imports, just copy pasted.
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.database.Cursor
import kotlin.collections.MutableList
import android.view.VelocityTracker
import com.example.musicapp.AudioFile
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class AudioAdapter(
    private val audioList: List<AudioFile>,
    private val onItemClick: (AudioFile) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.audioTitle)
        val artist: TextView = itemView.findViewById(R.id.audioArtist)
        val thumbnail: ImageView = itemView.findViewById(R.id.audioThumbnail)

        fun bind(audio: AudioFile, onItemClick: (AudioFile) -> Unit) {
            title.text = audio.title
            artist.text = audio.artist
            itemView.setOnClickListener { onItemClick(audio) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position], onItemClick)
    }

    override fun getItemCount(): Int = audioList.size
}
