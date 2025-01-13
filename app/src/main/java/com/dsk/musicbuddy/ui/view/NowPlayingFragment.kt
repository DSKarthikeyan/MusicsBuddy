package com.dsk.musicbuddy.ui.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.ui.MusicBuddyApplication
import com.dsk.musicbuddy.ui.viewmodel.GenericViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlayListFragment : Fragment(R.layout.activity_playlist) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAudioPlayList)
        val fab = view.findViewById<FloatingActionButton>(R.id.floatingButtonAddPlayList)

        fab.setOnClickListener {
        }
    }
}