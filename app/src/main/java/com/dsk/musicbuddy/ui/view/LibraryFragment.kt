package com.dsk.musicbuddy.ui.view

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.data.model.Song
import com.dsk.musicbuddy.databinding.ActivityAudioBinding
import com.dsk.musicbuddy.ui.MusicBuddyApplication
import com.dsk.musicbuddy.ui.adapter.SongListAdapter
import com.dsk.musicbuddy.ui.viewmodel.GenericViewModelFactory
import com.dsk.musicbuddy.ui.viewmodel.MusicPlayerViewModel
import com.dsk.musicbuddy.util.MusicService

class LibraryFragment : Fragment() {

    private var _binding: ActivityAudioBinding? = null
    private val binding get() = _binding!!

//    private val playerViewModel: PlaylistViewModel by viewModels {
//        GenericViewModelFactory { PlaylistViewModel(requireActivity().application as MusicBuddyApplication) }
//    }

    private val musicLibraryViewModel: MusicPlayerViewModel by viewModels {
        GenericViewModelFactory { MusicPlayerViewModel((requireActivity().application as MusicBuddyApplication).applicationContext) }
    }

    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var songListAdapter : SongListAdapter

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            musicLibraryViewModel.bindService(musicService!!)
            songListAdapter.bindMusicService(musicService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicLibraryViewModel.unbindService()
            musicService = null
        }
    }

    override fun onResume() {
        super.onResume()
        bindMusicService()
    }

    private fun bindMusicService() {
        Intent(requireContext(), MusicService::class.java).also { intent ->
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    override fun onStart() {
        super.onStart()
        bindMusicService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMusicBar: View? = activity?.findViewById(R.id.currentMusicBar)
        val playPauseImageView: ImageView? = currentMusicBar?.findViewById(R.id.imageViewPause)
        val songNameTextName: TextView? = currentMusicBar?.findViewById(R.id.textViewSongName)
        val albumNameTextView: TextView? = currentMusicBar?.findViewById(R.id.textViewAlbumName)

        songListAdapter = SongListAdapter(
            onSongClick = { song ->
                musicLibraryViewModel.playSong(song)
                currentMusicBar?.visibility = View.VISIBLE
            },
            onDeleteSong = { song ->
                showDeleteSongDialog(song)
            },
            onAddToPlaylist = { song ->
                handleAddToPlaylist(song)
            },
            editPlaylistName = { song ->
//                editPlaylistName(song, requireContext())
            }
        )

        binding.recyclerViewAudio.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songListAdapter
        }

        musicLibraryViewModel.songList.observe(viewLifecycleOwner) { songs ->
            if (songs.isNotEmpty()) {
                songListAdapter.submitList(songs)
            } else {
                binding.textViewNoDataPlayList.visibility = View.VISIBLE
                currentMusicBar?.visibility = View.GONE
            }
        }

        musicLibraryViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song == null) {
                currentMusicBar?.visibility = View.GONE
            } else {
                currentMusicBar?.visibility = View.VISIBLE
                songNameTextName?.text = song.title
                albumNameTextView?.text = song.artist
                playPauseImageView?.setImageResource(
                    if (musicLibraryViewModel.isPlaying()) R.drawable.ic_pause_white
                    else R.drawable.ic_play_white
                )
            }
        }

        musicLibraryViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying == null) {
                currentMusicBar?.visibility = View.GONE
            } else {
                playPauseImageView?.setImageResource(
                    if (isPlaying) R.drawable.ic_pause_white
                    else R.drawable.ic_play_white
                )
            }
        }

        playPauseImageView?.setOnClickListener {
            if (musicLibraryViewModel.isPlaying()) {
                musicLibraryViewModel.pauseSong()
            } else {
                musicLibraryViewModel.resumeSong()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            requireActivity().unbindService(serviceConnection)
            isBound = false
        }
        _binding = null
    }

    private fun showDeleteSongDialog(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Song")
            .setMessage("Are you sure you want to delete '${song.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                musicLibraryViewModel.deleteSong(song)
                Toast.makeText(requireContext(), "'${song.title}' deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

//    private fun editPlaylistName(song: Song, context: Context) {
//        val input = EditText(context).apply { hint = "Enter new playlist name" }
//        Log.d("DsK","editPlaylistName")
//        AlertDialog.Builder(context)
//            .setTitle("Edit Playlist Name")
//            .setView(input)
//            .setPositiveButton("Save") { _, _ ->
//                val newPlaylistName = input.text.toString().trim()
//                if (newPlaylistName.isNotEmpty()) {
//                    playerViewModel.renamePlaylist(song.id, newPlaylistName)
//                } else {
//                    Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

    private fun handleAddToPlaylist(song: Song) {
//        val playlists = playerViewModel.playlists.value

//        if (playlists.isNullOrEmpty()) {
//            showCreatePlaylistDialog(song)
//        } else {
//            showPlaylistSelectionDialog(song, playlists)
//        }
    }

//    private fun showPlaylistSelectionDialog(song: Song, playlists: List<Playlist>) {
//        val playlistNames = playlists.map { it.name }.toTypedArray()
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Select Playlist")
//            .setItems(playlistNames) { _, which ->
//                val selectedPlaylist = playlists[which]
//                playerViewModel.addSongToPlaylist(selectedPlaylist.id, song)
//                Toast.makeText(
//                    requireContext(),
//                    "Added to ${selectedPlaylist.name}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            .setNegativeButton("Create New Playlist") { _, _ ->
//                showCreatePlaylistDialog(song)
//            }
//            .show()
//    }
//
//    private fun showCreatePlaylistDialog(song: Song) {
//        val input = EditText(requireContext())
//        input.hint = "Enter playlist name"
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Create Playlist")
//            .setView(input)
//            .setPositiveButton("Create") { _, _ ->
//                val playlistName = input.text.toString().trim()
//                if (playlistName.isNotEmpty()) {
//                    playerViewModel.createPlaylist(playlistName)
//                    Toast.makeText(requireContext(), "Playlist created", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(requireContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
}

