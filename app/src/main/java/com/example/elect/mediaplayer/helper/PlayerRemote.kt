package com.example.elect.mediaplayer.helper

import android.app.Activity
import android.content.*
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.service.MusicService
import java.util.*

object PlayerRemote {

    var musicService: MusicService? = null
    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    @JvmStatic
    val isPlaying: Boolean
        get() = musicService != null &&
                musicService!!.isPlaying


    fun isPlaying(media: Media): Boolean {

        return if (!isPlaying) {
            false
        }

        else media.id == currentMedia.id
    }

    val playingQueue: List<Media>
        get() = if(musicService != null) {
            musicService?.playingQueue as List<Media>
        } else listOf()

    var position: Int
        get() = if(musicService != null){
            musicService!!.position
        } else -1
        set(position) {
            if(musicService != null){
                musicService!!.position = position
            }
        }

    val currentMedia: Media
        get() = if (musicService != null) {
            musicService!!.currentMedia
        } else Media.emptyMedia

    val nextMedia: Media?
        get() = if (musicService != null) {
            musicService?.nextMedia
        } else Media.emptyMedia

    val mediaDurationMillis: Long
        get() = if (musicService != null) {
            musicService!!.mediaDurationMillis
        } else -1

    val mediaProgressMillis: Long
        get() = if (musicService != null) {
            musicService!!.mediaProgressMillis
        } else -1

    val repeatMode: Int
        get() = if (musicService != null) {
            musicService!!.repeatMode
        } else MusicService.REPEAT_MODE_NONE

    @JvmStatic
    val shuffleMode: Int
        get() = if (musicService != null) {
            musicService!!.shuffleMode
        } else MusicService.SHUFFLE_MODE_NONE


    val audioSessionId: Int
        get() = if(musicService != null) {
            musicService!!.audioSessionId
        } else -1


    fun bindToService(
        context: Context,
        callback: ServiceConnection
    ): ServiceToken? {

        var realActivity: Activity? =
            (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }

        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(
            contextWrapper,
            MusicService::class.java
        )

        try {

            contextWrapper.startService(intent)
        } catch (ignored: IllegalStateException) {

            ContextCompat.startForegroundService(context, intent)
        }

        val binder = ServiceBinder(callback)


        if (

            contextWrapper.bindService(
                Intent().setClass(
                    contextWrapper,
                    MusicService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }

        val mContextWrapper = token.mWrappedContext

        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return

        mContextWrapper.unbindService(mBinder)

        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    @JvmStatic
    fun openAndShuffleQueue(
        queue: List<Media>,
        startPlaying: Boolean
    ) {

        var startPosition = 0

        if (queue.isNotEmpty()) {

            startPosition = Random().nextInt(queue.size)
        }


        if (!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            openQueue(queue, startPosition, startPlaying)
            setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE)
        }
    }

    fun setShuffleMode(shuffleMode: Int): Boolean {

        if (musicService != null) {

            musicService!!.shuffleMode = shuffleMode
            return true
        }
        return false
    }


    @JvmStatic
    fun openQueue(
        queue: List<Media>,
        startPosition: Int,
        startPlaying: Boolean
    ) {

        if(!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            musicService?.openQueue(
                queue,
                startPosition,
                startPlaying
            )
        }
    }

    private fun tryToHandleOpenPlayingQueue(
        queue: List<Media>,
        startPosition: Int,
        startPlaying: Boolean
    ): Boolean {

        return false
    }


    fun playMediaAt(position: Int) {
        musicService?.playMediaAt(position)
    }

    fun playNextMedia() {
        musicService?.playNextMedia()
    }


    fun playPreviousMedia() {
        musicService?.back()
    }

    fun pauseMedia() {
        musicService?.pause()
    }

    fun resumeMedia() {
        musicService?.play()
    }

    fun quit() {
        musicService?.quit()
    }

    fun seekTo(millis: Long): Long {
        return if (musicService != null) {
            musicService!!.seek(millis)
        } else -1
    }

    fun cycleRepeatMode(): Boolean {
        if (musicService != null) {
            musicService?.cycleRepeatMode()
            return true
        }
        return false
    }

    fun toggleShuffleMode(): Boolean {
        if (musicService != null) {

            musicService?.toggleShuffle()
            return true
        }
        return false
    }

    fun clearQueue(): Boolean {
        if (musicService != null) {
            musicService!!.clearQueue()
            return true
        }
        return false
    }

    fun stopForegroundAndNotification() {
        musicService?.stopForegroundAndNotification()
    }

    fun stopNotification(){
        musicService?.stopNotification()
    }


    class ServiceBinder internal constructor(

        private val mCallback: ServiceConnection?
    ) : ServiceConnection {


        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as MusicService.MusicBinder

            musicService = binder.service


            mCallback?.onServiceConnected(className, service)
        }


        override fun onServiceDisconnected(
            className: ComponentName
        ) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }
    }

    class ServiceToken internal constructor(
        internal var mWrappedContext: ContextWrapper
    )
}