package com.example.elect.mediaplayer.service

interface Playback {

    val isInitialized: Boolean

    val isPlaying: Boolean

    val audioSessionId: Int

    fun setDataSource(path: String): Boolean

    fun setNextDataSource(path: String?)

    fun setCallbacks(callbacks: PlaybackCallbacks)

    fun start(): Boolean

    fun stop()

    fun release()

    fun pause(): Boolean


    fun duration(): Long


    fun position(): Long

    fun seek(whereto: Long): Long

    fun setVolume(vol: Float): Boolean

    fun setAudioSessionId(sessionId: Int): Boolean

    interface PlaybackCallbacks {
        fun onMediaWentToNext()

        fun onMediaEnded()
    }
}