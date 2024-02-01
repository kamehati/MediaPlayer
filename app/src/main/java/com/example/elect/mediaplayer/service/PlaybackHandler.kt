package com.example.elect.mediaplayer.service

import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.elect.mediaplayer.service.MusicService.Companion.DUCK
import com.example.elect.mediaplayer.service.MusicService.Companion.FOCUS_CHANGE
import com.example.elect.mediaplayer.service.MusicService.Companion.MEDIA_ENDED
import com.example.elect.mediaplayer.service.MusicService.Companion.MEDIA_WENT_TO_NEXT
import com.example.elect.mediaplayer.service.MusicService.Companion.META_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.NOT_PLAY_MEDIA
import com.example.elect.mediaplayer.service.MusicService.Companion.PLAY_MEDIA
import com.example.elect.mediaplayer.service.MusicService.Companion.PLAY_STATE_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.PREPARE_NEXT
import com.example.elect.mediaplayer.service.MusicService.Companion.RELEASE_WAKELOCK
import com.example.elect.mediaplayer.service.MusicService.Companion.REPEAT_MODE_NONE
import com.example.elect.mediaplayer.service.MusicService.Companion.RESTORE_QUEUES
import com.example.elect.mediaplayer.service.MusicService.Companion.SET_POSITION
import com.example.elect.mediaplayer.service.MusicService.Companion.STOP_NOTIFICATION
import com.example.elect.mediaplayer.service.MusicService.Companion.UNDUCK
import java.lang.ref.WeakReference

class PlaybackHandler(
    private val service: MusicService,
    looper: Looper
): Handler(looper) {

    override fun handleMessage(msg: Message) {
        val isDuck = false
        var currentDuckVolume = 1.0f
        val mService = WeakReference(service)
        val service = mService.get() ?: return

        when(msg.what) {

            PLAY_MEDIA -> {
                service.playMediaAtImpl(msg.arg1)
            }

            STOP_NOTIFICATION -> {
                service.stopForegroundAndNotification()
            }

            SET_POSITION -> {
                service.openTrackAndPrepareNextAt(msg.arg1)
                service.handleAndSendChangeInternal(PLAY_STATE_CHANGED)
            }

            PREPARE_NEXT -> {

                service.prepareNextImpl()
            }

            RESTORE_QUEUES -> {
                service.restoreQueuesAndPositionIfNecessary()
            }


            MEDIA_WENT_TO_NEXT -> {
                if (

                    service.repeatMode == REPEAT_MODE_NONE &&
                    service.isLastTrack
                ) {
                    service.pause()
                    service.seek(0)
                } else {
                    service.position = service.nextPosition
                    service.prepareNextImpl()

                    service.handleAndSendChangeInternal(META_CHANGED)
                }
            }

            MEDIA_ENDED -> {
                if (
                    service.repeatMode == REPEAT_MODE_NONE &&
                    service.isLastTrack
                ) {
                    service.handleAndSendChangeInternal(PLAY_STATE_CHANGED)
                    service.seek(0)
                } else {
                    service.playNextMedia()
                }
                sendEmptyMessage(RELEASE_WAKELOCK)
            }

            RELEASE_WAKELOCK -> {
                service.releaseWakeLock()
            }

            DUCK -> {
                if (isDuck) {

                    currentDuckVolume -= .05f

                    if (currentDuckVolume > .2f) {

                        sendEmptyMessageDelayed(DUCK, 10)
                    } else {

                        currentDuckVolume = .2f
                    }
                } else {

                    currentDuckVolume = 1f
                }
                service.playback?.setVolume(currentDuckVolume)
            }

            UNDUCK -> {
                if (isDuck) {

                    currentDuckVolume += .03f

                    if (currentDuckVolume < 1f) {

                        sendEmptyMessageDelayed(
                            UNDUCK,
                            10)
                    } else {

                        currentDuckVolume = 1f
                    }
                } else {

                    currentDuckVolume = 1f
                }


                service.playback?.setVolume(currentDuckVolume)
            }

            FOCUS_CHANGE -> {
                when (msg.arg1) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (
                            !service.isPlaying &&
                            service.isPausedByTransientLossOfFocus
                        ) {
                            service.play()
                            service.isPausedByTransientLossOfFocus = false
                        }

                        removeMessages(DUCK)
                        sendEmptyMessage(UNDUCK)
                    }

                    AudioManager.AUDIOFOCUS_LOSS ->
                        service.forcePause()

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {


                        val wasPlaying: Boolean = service.isPlaying
                        service.forcePause()
                        service.isPausedByTransientLossOfFocus = wasPlaying
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {

                        removeMessages(UNDUCK)
                        sendEmptyMessage(DUCK)
                    }
                }
            }
        }
    }
}