package com.example.elect.mediaplayer.helper

import android.os.Handler
import android.os.Message
import kotlin.math.max

class MusicProgressViewUpdateHelper : Handler {

    interface Callback {

        fun onUpdateProgressViews(progress: Long, total: Long)
    }

    private var callback: Callback? = null
    private var intervalPlaying: Long = 0
    private var intervalPaused: Long = 0

    constructor(callback: Callback) {
        this.callback = callback
        this.intervalPlaying = UPDATE_INTERVAL_PLAYING
        this.intervalPaused = UPDATE_INTERVAL_PAUSED
    }


    constructor(callback: Callback,
                intervalPlaying: Long,
                intervalPaused: Long) {
        this.callback = callback
        this.intervalPlaying = intervalPlaying
        this.intervalPaused = intervalPaused
    }


    fun start() {

        queueNextRefresh(1)
    }


    fun stop() {
        removeMessages(CMD_REFRESH_PROGRESS_VIEWS)
    }


    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == CMD_REFRESH_PROGRESS_VIEWS) {

            queueNextRefresh(

                refreshProgressViews()
            )
        }
    }


    private fun refreshProgressViews(): Long {

        val progressMillis = PlayerRemote.mediaProgressMillis

        val totalMillis = PlayerRemote.mediaDurationMillis

        if (totalMillis > 0)

            callback?.onUpdateProgressViews(
                progressMillis,
                totalMillis
            )


        if (!PlayerRemote.isPlaying) {

            return intervalPaused
        }


        val remainingMillis =
            intervalPlaying - progressMillis % intervalPlaying


        return max(MIN_INTERVAL, remainingMillis)
    }


    private fun queueNextRefresh(delay: Long) {
        val message = obtainMessage(CMD_REFRESH_PROGRESS_VIEWS)
        removeMessages(CMD_REFRESH_PROGRESS_VIEWS)

        sendMessageDelayed(message, delay)
    }

    companion object {
        private const val CMD_REFRESH_PROGRESS_VIEWS = 1
        private const val MIN_INTERVAL = 20L
        private const val UPDATE_INTERVAL_PLAYING = 1000L
        private const val UPDATE_INTERVAL_PAUSED = 500L
    }
}
