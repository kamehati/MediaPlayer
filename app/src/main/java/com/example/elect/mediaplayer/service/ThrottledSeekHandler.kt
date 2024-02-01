package com.example.elect.mediaplayer.service

import android.os.Handler
import com.example.elect.mediaplayer.service.MusicService.Companion.PLAY_STATE_CHANGED

class ThrottledSeekHandler(
    private val musicService: MusicService,
    private val handler: Handler
) : Runnable {

    fun notifySeek() {

        musicService.updateMediaSessionPlaybackState()

        musicService.updateMediaSessionMetaData()

        handler.removeCallbacks(this)

        handler.postDelayed(this, THROTTLE)
    }

    override fun run() {

        musicService.savePositionInTrack()
    }

    companion object {

        private const val THROTTLE: Long = 500
    }
}