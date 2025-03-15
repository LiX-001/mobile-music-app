package com.example.musicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class QueueAdapter(
    private val queueList: MutableList<AudioFile>,
    private val onItemClick: (AudioFile) -> Unit
) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.queueTitle)
        val artist: TextView = itemView.findViewById(R.id.queueArtist)
        val thumbnail: ImageView = itemView.findViewById(R.id.queueThumbnail)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeFromQueue)

        fun bind(audio: AudioFile, onItemClick: (AudioFile) -> Unit, onRemoveClick: (AudioFile) -> Unit) {
            title.text = audio.title
            artist.text = audio.artist
            itemView.setOnClickListener { onItemClick(audio) }
            removeButton.setOnClickListener { onRemoveClick(audio) }

            Glide.with(itemView.context)
                .load(audio.thumbnail)
                .placeholder(R.drawable.random_1000x1000) // Add a placeholder image
                .into(thumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_queue, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        holder.bind(queueList[position], onItemClick) { audio ->
            queueList.remove(audio)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = queueList.size
}
