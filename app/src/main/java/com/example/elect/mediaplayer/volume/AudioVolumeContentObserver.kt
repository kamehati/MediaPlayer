package com.example.elect.mediaplayer.volume

import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Handler

class AudioVolumeContentObserver(
    handler: Handler,
    audioManager: AudioManager,
    audioStreamType: Int,
    listener: OnAudioVolumeChangedListener
): ContentObserver(handler) {

    private val mAudioManger = audioManager
    private val mAudioStreamType = audioStreamType
    private val mListener = listener
    private var mLastVolume = audioManager.getStreamVolume(mAudioStreamType)

    override fun onChange(
        selfChange: Boolean,
        uri: Uri?
    ) {
        super.onChange(selfChange, uri)


        val maxVolume: Int = mAudioManger.getStreamMaxVolume(
            mAudioStreamType
        )

        val currentVolume: Int = mAudioManger.getStreamVolume(
            mAudioStreamType
        )

        if (currentVolume != mLastVolume) {



            mLastVolume = currentVolume

            mListener.onAudioVolumeChanged(
                currentVolume,
                maxVolume
            )
        }
    }


    override fun deliverSelfNotifications(): Boolean {
        return super.deliverSelfNotifications()
    }
}