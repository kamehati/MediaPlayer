package com.example.elect.mediaplayer.activity.base

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.interfaces.IMusicServiceEventListener
import com.example.elect.mediaplayer.service.MusicService.Companion.ACTION_QUIT
import com.example.elect.mediaplayer.service.MusicService.Companion.FAVORITE_STATE_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.META_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.PLAY_STATE_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.QUEUE_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.REPEAT_MODE_CHANGED
import com.example.elect.mediaplayer.service.MusicService.Companion.SHUFFLE_MODE_CHANGED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

abstract class BaseMusicServiceActivity:
    AppCompatActivity(),
    IMusicServiceEventListener {

    private val mMusicServiceEventListeners =
        ArrayList<IMusicServiceEventListener>()

    private var serviceToken:
            PlayerRemote.ServiceToken? = null

    private var musicStateReceiver: MusicStateReceiver? = null

    private var receiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceToken = PlayerRemote.bindToService(
            this,
            object : ServiceConnection{
                override fun onServiceConnected(
                    name: ComponentName,
                    service: IBinder
                ) {
                    this@BaseMusicServiceActivity.onServiceConnected()
                }

                override fun onServiceDisconnected(
                    name: ComponentName
                ) {
                    this@BaseMusicServiceActivity.onServiceDisconnected()
                }

            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        PlayerRemote.unbindFromService(serviceToken)

        if(receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
    }


    fun addMusicServiceEventListener(
        listenerI: IMusicServiceEventListener?
    ) {
        if (listenerI != null) {

            mMusicServiceEventListeners.add(listenerI)
        }
    }

    fun removeMusicServiceEventListener(
        listenerI: IMusicServiceEventListener?
    ) {
        if (listenerI != null) {
            mMusicServiceEventListeners.remove(listenerI)
        }
    }

    override fun onServiceConnected() {
        if(!receiverRegistered){
            musicStateReceiver = MusicStateReceiver(this)

            val filter = IntentFilter()
            filter.addAction(FAVORITE_STATE_CHANGED)
            filter.addAction(META_CHANGED)
            filter.addAction(QUEUE_CHANGED)
            filter.addAction(PLAY_STATE_CHANGED)
            filter.addAction(REPEAT_MODE_CHANGED)
            filter.addAction(SHUFFLE_MODE_CHANGED)

            registerReceiver(musicStateReceiver, filter)

            receiverRegistered = true
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if(receiverRegistered){
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    override fun onFavoriteStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onFavoriteStateChanged()
        }
    }

    private class MusicStateReceiver(
        activity: BaseMusicServiceActivity
    ) : BroadcastReceiver() {

        private val reference
        : WeakReference<BaseMusicServiceActivity>
        = WeakReference(activity)

        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            val activity = reference.get()

            if (activity != null && action != null) {

                when (action) {
                    FAVORITE_STATE_CHANGED -> activity.onFavoriteStateChanged()
                    META_CHANGED -> activity.onPlayingMetaChanged()
                    QUEUE_CHANGED -> activity.onQueueChanged()

                    PLAY_STATE_CHANGED -> activity.onPlayStateChanged()

                    REPEAT_MODE_CHANGED -> activity.onRepeatModeChanged()

                    SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()

                }
            }
        }
    }
}