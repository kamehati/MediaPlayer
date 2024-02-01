package com.example.elect.mediaplayer.adapter.music.album

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song

class AlbumSongAdapter(
    override val activity: FragmentActivity,
    override var dataSet: MutableList<Media>,
    override var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    override val fragment: Fragment
): SongAdapter(
    activity, dataSet, itemLayoutRes, ICabHolder, fragment, R.menu.cab_holder_menu
) {
}