package com.example.elect.mediaplayer.volume

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.getSystemService

class AudioVolumeObserver(private val context: Context) {


    private val mAudioManager: AudioManager =
        context.getSystemService()!!
    private var contentObserver: AudioVolumeContentObserver? = null


    fun register(
        audioStreamType: Int,
        listener: OnAudioVolumeChangedListener
    ) {
        val handler = Handler(Looper.getMainLooper())

        contentObserver = AudioVolumeContentObserver(
            handler,
            mAudioManager,
            audioStreamType,
            listener
        )

        context.contentResolver.registerContentObserver(

            Settings.System.CONTENT_URI,

            true,

            contentObserver!!
        )
    }


    fun unregister() {
        if (contentObserver != null) {

            context.contentResolver.unregisterContentObserver(
                contentObserver!!
            )
            contentObserver = null
        }
    }
}