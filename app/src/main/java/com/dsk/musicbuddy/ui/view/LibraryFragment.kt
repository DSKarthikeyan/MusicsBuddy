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

    private val musicLibraryViewModel: MusicPlayerViewModel by viewModels {
        GenericViewModelFactory {
            MusicPlayerViewModel((requireActivity().application as MusicBuddyApplication).applicationContext)
        }
    }

    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var songListAdapter: SongListAdapter
    private lateinit var currentMusicBar: View

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
        setupRecyclerView()
        setupObservers()
        setupCurrentMusicBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindMusicService()
        _binding = null
    }

    private fun bindMusicService() {
        Intent(requireContext(), MusicService::class.java).also { intent ->
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    private fun unbindMusicService() {
        if (isBound) {
            requireActivity().unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupRecyclerView() {
        currentMusicBar = activity?.findViewById(R.id.currentMusicBar)!!
        songListAdapter = SongListAdapter(
            onSongClick = { song ->
                musicLibraryViewModel.playSong(song)
                currentMusicBar?.visibility = View.VISIBLE
            },
            onDeleteSong = { song -> showDeleteSongDialog(song) },
            onAddToPlaylist = { song -> handleAddToPlaylist(song) },
            editPlaylistName ={ song ->  editPlaylistName(song, requireContext())}
        )

        binding.recyclerViewAudio.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songListAdapter
        }
    }

    private fun setupObservers() {
        musicLibraryViewModel.songList.observe(viewLifecycleOwner) { songs ->
            binding.textViewNoDataPlayList.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            songListAdapter.submitList(songs)
        }

        musicLibraryViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            updateCurrentMusicBar(song)
        }

        musicLibraryViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            updatePlayPauseIcon(isPlaying)
        }
    }

    private fun setupCurrentMusicBar() {
        val playPauseImageView = currentMusicBar.findViewById<ImageView>(R.id.imageViewPause)

        playPauseImageView.setOnClickListener {
            if (musicLibraryViewModel.isPlaying()) {
                musicLibraryViewModel.pauseSong()
            } else {
                musicLibraryViewModel.resumeSong()
            }
        }
    }

    private fun updateCurrentMusicBar(song: Song?) {
        currentMusicBar.visibility = if (song == null) View.GONE else View.VISIBLE

        if (song != null) {
            currentMusicBar.findViewById<TextView>(R.id.textViewSongName).text = song.title
            currentMusicBar.findViewById<TextView>(R.id.textViewAlbumName).text = song.artist
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean?) {
        val playPauseImageView = currentMusicBar.findViewById<ImageView>(R.id.imageViewPause)
        isPlaying?.let {
            playPauseImageView.setImageResource(
                if (it) R.drawable.ic_pause_white else R.drawable.ic_play_white
            )
        }
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

    private fun editPlaylistName(song: Song, context: Context) {
        val input = EditText(context).apply { hint = "Enter new playlist name" }
        Log.d("DsK","editPlaylistName")
        AlertDialog.Builder(context)
            .setTitle("Edit Playlist Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPlaylistName = input.text.toString().trim()
                if (newPlaylistName.isNotEmpty()) {
//                    playerViewModel.renamePlaylist(song.id, newPlaylistName)
                } else {
                    Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleAddToPlaylist(song: Song) {
//        val playlists = playerViewModel.playlists.value
//
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
////                playerViewModel.addSongToPlaylist(selectedPlaylist.id, song)
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

