package com.dsk.musicbuddy.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.dsk.musicbuddy.databinding.SongDetailViewBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.data.model.Song
import com.dsk.musicbuddy.databinding.AudioDetailViewBinding
import com.dsk.musicbuddy.util.MusicService
import com.dsk.musicbuddy.util.Utility

class SongListAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onDeleteSong: (Song) -> Unit,
    private val onAddToPlaylist: (Song) -> Unit,
    private val editPlaylistName: (Song) -> Unit
) : ListAdapter<Song, SongListAdapter.SongViewHolder>(SongDiffCallback()) {
    private var musicService: MusicService? = null

    fun bindMusicService(service: MusicService) {
        musicService = service
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = AudioDetailViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1) // Pass position for song count
    }

    inner class SongViewHolder(private val binding: AudioDetailViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, songCount: Int) {
            binding.textViewSongName.text = song.title
            binding.textViewAlbumName.text = song.artist
            binding.textViewSongDuration.text = formatDuration(song.duration)
            binding.textViewSongCount.text = songCount.toString() // Set song count

            // Set album art or fallback image
//            try {
//                if (song.albumArtUri != null) {
//                    Glide.with(binding.imageViewSongURI.context)
//                        .load(song.albumArtUri)
//                        .placeholder(R.drawable.ic_album) // Placeholder image
//                        .error(R.drawable.ic_album) // Fallback image on error
//                        .into(binding.imageViewSongURI)
//                } else {
//                    binding.imageViewSongURI.setImageResource(R.drawable.ic_album)
//                }
//            } catch (e: Exception) {
//                Log.e("SongListAdapter", "Failed to load album art: ${e.localizedMessage}")
//                binding.imageViewSongURI.setImageResource(R.drawable.ic_album)
//            }

            // Handle options click (e.g., show delete/add to playlist options)
            // Handle options click (show popup menu)
            binding.imageViewOptions.setOnClickListener { view ->
                showPopupMenu(view, song, binding.root.context)
            }

            // Handle item click
            binding.root.setOnClickListener {
                onSongClick(song)
            }
        }

        private fun showPopupMenu(view: View, song: Song, context: Context) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.song_list_menu) // Your XML menu file name

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.actionPlay -> {
                        onSongClick(song) // Play the song
                        true
                    }

                    R.id.editPlayListName -> {
                        Log.d("DsK","editPlayListName click")
                        editPlaylistName(song)
                        true
                    }

                    R.id.addToPlaylist -> {
                        Log.d("DsK","addPlayListName click")
                        onAddToPlaylist(song)
                        true
                    }

                    R.id.playNext -> {
                        queueSongNext(song, view.context)
                        true
                    }

                    R.id.share -> {
                        Utility.shareFiles(context,song.songUri)
                        true
                    }

                    R.id.delete -> {
                        showDeleteConfirmationDialog(song, context) // Show confirmation for delete
                        true
                    }

                    R.id.addToQueue -> {
                        addToQueue(song,context)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        private fun addToQueue(song: Song, context: Context) {
            musicService?.addToQueue(song) ?: Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show()
        }

        private fun queueSongNext(song: Song, context: Context) {
            musicService?.queueNext(song) ?: Toast.makeText(context, "Service not bound", Toast.LENGTH_SHORT).show()
        }

        private fun formatDuration(durationMillis: Long): String {
            val minutes = (durationMillis / 1000) / 60
            val seconds = (durationMillis / 1000) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        private fun showDeleteConfirmationDialog(song: Song, context: Context) {
            AlertDialog.Builder(context).setTitle("Delete Song")
                .setMessage("Are you sure you want to delete '${song.title}'?")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteSong(song) // Trigger delete callback
                }.setNegativeButton("Cancel", null).show()
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}
