package com.example.musicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AudioAdapter(
    private val audioList: List<AudioFile>,
    private val onItemClick: (AudioFile) -> Unit,
    private val onAddClick: (AudioFile) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var filteredList: List<AudioFile> = audioList

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.audioTitle)
        val artist: TextView = itemView.findViewById(R.id.audioArtist)
        val thumbnail: ImageView = itemView.findViewById(R.id.audioThumbnail)
        val addButton: ImageButton = itemView.findViewById(R.id.addButton)

        fun bind(audio: AudioFile, onItemClick: (AudioFile) -> Unit, onAddClick: (AudioFile) -> Unit) {
            title.text = audio.title
            artist.text = audio.artist
            Glide.with(itemView.context)
                .load(audio.thumbnail)
                .placeholder(R.drawable.random_1000x1000) // Add a placeholder image
                .into(thumbnail)
        
            itemView.setOnClickListener { onItemClick(audio) }
        
            addButton.setOnClickListener { 
                onAddClick(audio) 
            }
    }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position], onItemClick, onAddClick)
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            audioList  // Reset to full list if query is empty
        } else {
            audioList.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist?.contains(query, ignoreCase = true) == true
            }
        }
        notifyDataSetChanged() // Refresh RecyclerView
    }
}
