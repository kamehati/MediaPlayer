package com.example.elect.mediaplayer.interfaces

import com.example.elect.mediaplayer.db.PlaylistWithMedias

interface IPlaylistClickListener {
    fun onPlaylistClick(playlist: PlaylistWithMedias)
}