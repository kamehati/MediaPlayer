package com.example.elect.mediaplayer.auto

import android.content.Context
import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import com.example.elect.mediaplayer.activity.Category
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.repository.SongRepository
import com.example.elect.mediaplayer.service.MusicService
import java.lang.ref.WeakReference

class AutoMusicProvider(
    val mContext: Context,
    private val songsRepository: SongRepository
) {
    private var mMusicService: WeakReference<MusicService>? = null

    fun setMusicService(service: MusicService) {
        mMusicService = WeakReference(service)
    }

    fun getChildren(
        mediaId: String?,
        resources: Resources
    ): List<MediaBrowserCompat.MediaItem> {
        val mediaItems: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
        when (mediaId) {
            MusicService.MEDIA_ID_ROOT -> {
                mediaItems.addAll(getRootChildren(resources))
            }
            else -> {
                getPlaylistChildren(mediaId, mediaItems)
            }
        }

        return mediaItems
    }

    private fun getRootChildren(
        resources: Resources
    ): List<MediaBrowserCompat.MediaItem> {
        val mediaItems: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
        val libraryCategories = listOf(Category.Videos, Category.Musics, Category.Favorites, Category.Playlists)

        libraryCategories.forEach {
            when(it.number) {
                0 -> {}
                1 -> {}
                2 -> {}
                3 -> {}
            }
        }

        return mediaItems
    }

    private fun getPlaylistChildren(
        mediaId: String?,
        mediaItems: MutableList<MediaBrowserCompat.MediaItem>
    ) {}
}