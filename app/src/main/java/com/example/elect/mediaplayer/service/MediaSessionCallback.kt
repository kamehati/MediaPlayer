package com.example.elect.mediaplayer.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.example.elect.mediaplayer.helper.PlayerRemote.cycleRepeatMode
import com.example.elect.mediaplayer.service.MusicService.Companion.CYCLE_REPEAT
import com.example.elect.mediaplayer.service.MusicService.Companion.TOGGLE_FAVORITE
import com.example.elect.mediaplayer.service.MusicService.Companion.TOGGLE_SHUFFLE
import org.koin.core.component.KoinComponent


class MediaSessionCallback(
    private val context: Context,
    private val musicService: MusicService
): MediaSessionCompat.Callback(),
    KoinComponent {


    override fun onPrepare() {
        super.onPrepare()
    }


    override fun onPlay() {
        super.onPlay()
        musicService.play()
    }


    override fun onPause() {
        super.onPause()
        musicService.pause()
    }


    override fun onSkipToNext() {
        super.onSkipToNext()
        musicService.playNextMedia()
    }


    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        musicService.back()
    }


    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)

        musicService.seek(pos)
    }


    override fun onStop() {
        super.onStop()
        musicService.quit()
    }


    override fun onMediaButtonEvent(
        mediaButtonEvent: Intent
    ): Boolean {
        return MediaButtonIntentReceiver.handleIntent(
            context,
            mediaButtonEvent
        )
    }


    override fun onCustomAction(
        action: String,
        extras: Bundle?
    ) {
        when (action) {
            CYCLE_REPEAT -> {
                cycleRepeatMode()

            }

            TOGGLE_SHUFFLE -> {
                musicService.toggleShuffle()

            }

            TOGGLE_FAVORITE -> {

            }

            else -> {
                println("Unsupported action: $action")
            }
        }
    }
}