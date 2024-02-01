package com.example.elect.mediaplayer.adapter.music

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.fragments.music.album.AlbumFragment
import com.example.elect.mediaplayer.fragments.music.artist.ArtistFragment
import com.example.elect.mediaplayer.fragments.music.folder.SongFolderFragment
import com.example.elect.mediaplayer.fragments.music.song.SongFragment
import com.example.elect.mediaplayer.views.TopAppBarLayout

class MusicTabPagerAdapter(
    private val topAppBarLayout: TopAppBarLayout,
    activity: MainActivity
    ) : FragmentStateAdapter(activity){

    private var songFragment = SongFragment()
    private var songFolderFragment = SongFolderFragment()
    private var albumFragment = AlbumFragment()
    private var artistFragment = ArtistFragment()

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> songFragment
            1 -> songFolderFragment
            2 -> albumFragment
            3 -> artistFragment
            else -> Fragment()
        }
    }

    fun bottomNavSelected(position: Int) {
        when(position){
            0 -> songFragment.bottomNavSelect(topAppBarLayout)
            1 -> songFolderFragment.bottomNavSelect(topAppBarLayout)
            2 -> albumFragment.bottomNavSelect(topAppBarLayout)
            3 -> artistFragment.bottomNavSelect(topAppBarLayout)
            else -> return
        }
    }
}