package com.example.elect.mediaplayer.fragments.player

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.service.MusicService
import kotlinx.coroutines.*

class MusicSkipTouchListener(
    val activity: FragmentActivity,
    val isNext: Boolean
) : View.OnTouchListener{

    var job: Job? = null
    var counter = 0
    var wasSeeking = false

    override fun onTouch(
        v: View?,
        event: MotionEvent?
    ): Boolean {
        val action = event?.actionMasked

        if (
            action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_CANCEL
        ) {
            job?.cancel()

            if (!wasSeeking) {

                val isNextSongOrVideo = if(PlayerRemote.nextMedia == null){
                    when(PlayerRemote.repeatMode) {
                        MusicService.REPEAT_MODE_ALL -> {
                            PlayerRemote.playingQueue[0].isSongOrVideo
                        }
                        MusicService.REPEAT_MODE_NONE -> {
                            PlayerRemote.currentMedia.isSongOrVideo
                        }
                        else -> {
                            Media.emptyMedia.isSongOrVideo
                        }
                    }
                } else {
                    PlayerRemote.nextMedia!!.isSongOrVideo
                }

                val isPreviousSongOrVideo =
                    when(PlayerRemote.repeatMode) {
                        MusicService.REPEAT_MODE_ALL -> {
                            if(PlayerRemote.position - 1 < 0){
                                PlayerRemote.playingQueue[
                                        PlayerRemote.playingQueue.size - 1
                                ].isSongOrVideo
                            } else {
                                PlayerRemote.playingQueue[
                                        PlayerRemote.position - 1
                                ].isSongOrVideo
                            }
                        }
                        MusicService.REPEAT_MODE_NONE -> {
                            if(PlayerRemote.position - 1 < 0){
                                PlayerRemote.currentMedia.isSongOrVideo
                            } else {
                                PlayerRemote.playingQueue[
                                        PlayerRemote.position - 1
                                ].isSongOrVideo
                            }
                        }
                        else -> {
                            Media.emptyMedia.isSongOrVideo
                        }
                    }

                if (isNext) {
                    PlayerRemote.playNextMedia()
                    if(
                        isNextSongOrVideo == 2
                    ) {
                        if(activity is PlayerActivity){
                            activity.createPlayerFragment(
                                isNextSongOrVideo
                            )
                        }
                    } else {

                    }
                } else {
                    if(PlayerRemote.mediaProgressMillis > 2000){
                        PlayerRemote.playPreviousMedia()
                    } else {
                        PlayerRemote.playPreviousMedia()

                        if(
                            isPreviousSongOrVideo == 2
                        ) {
                            if(activity is PlayerActivity){
                                activity.createPlayerFragment(
                                    isPreviousSongOrVideo
                                )
                            }
                        } else {
                        }
                    }
                }
            }
            wasSeeking = false
        }

        return if(event != null) gestureDetector.onTouchEvent(event) else false
    }

    private val gestureDetector = GestureDetector(
        activity,
        object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {

                job = activity.lifecycleScope.launch(
                    Dispatchers.Default
                ) {
                    counter = 0
                    while (isActive) {
                        delay(500)
                        wasSeeking = true
                        var seekingDuration = PlayerRemote.mediaProgressMillis

                        if (isNext) {
                            seekingDuration += 5000 * (counter.floorDiv(2) + 1)
                        } else {
                            seekingDuration -= 5000 * (counter.floorDiv(2) + 1)
                        }

                        PlayerRemote.seekTo(seekingDuration)
                        counter += 1
                    }
                }

                return super.onDown(e)
            }
        }
    )
}