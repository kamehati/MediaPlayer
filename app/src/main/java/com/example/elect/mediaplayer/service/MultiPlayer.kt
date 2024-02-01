package com.example.elect.mediaplayer.service

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.util.PreferenceUtil

class MultiPlayer(
    mContext: Context
) :
    Playback,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener{



    private var mCurrentMediaPlayer = MediaPlayer()
    private var mNextMediaPlayer : MediaPlayer? = null
    private var context: Context? = null

    private var callbacks : Playback.PlaybackCallbacks? = null

    private var mIsInitialized: Boolean = false

    override val isInitialized: Boolean
        get() = mIsInitialized

    override val audioSessionId: Int
        get() {
            return mCurrentMediaPlayer.audioSessionId
        }

    override val isPlaying: Boolean
        get() {
            return mIsInitialized &&
                    mCurrentMediaPlayer.isPlaying
        }

    init {
        this.context = mContext
        mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
    }

    override fun setDataSource(path: String): Boolean {

        mIsInitialized = false

        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path)

        if(mIsInitialized){
            setNextDataSource(null)
        }

        return mIsInitialized
    }

    private fun setDataSourceImpl(
        player: MediaPlayer,
        path: String
    ): Boolean {
        if(context == null){
            return false
        }

        try {
            player.reset()

            player.setOnPreparedListener(null)

            if(path.startsWith("content://")){
                player.setDataSource(
                    context!!,
                    Uri.parse(path)
                )
            } else {
                player.setDataSource(path)
            }

            player.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setLegacyStreamType(
                        AudioManager.STREAM_MUSIC
                    ).build()
            )

            player.prepare()
        } catch (e: Exception) {
            return false
        }

        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)

        val intent = Intent(
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
        )
        intent.putExtra(

            AudioEffect.EXTRA_AUDIO_SESSION,
            audioSessionId
        )
        intent.putExtra(

            AudioEffect.EXTRA_PACKAGE_NAME,
            context!!.packageName
        )
        intent.putExtra(

            AudioEffect.EXTRA_CONTENT_TYPE,

            AudioEffect.CONTENT_TYPE_MUSIC
        )

        context!!.sendBroadcast(intent)
        return true
    }

    override fun setNextDataSource(path: String?) {
        if(context == null){
            return
        }

        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null)
        } catch (e: IllegalArgumentException) {

            Log.i(TAG, "Next media player is current one, continuing");
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Media player not initialized!");
            return;
        }

        if(
            mNextMediaPlayer != null
        ){
            mNextMediaPlayer!!.release()
            mNextMediaPlayer = null
        }
        if(path == null){
            return
        }


        mNextMediaPlayer = MediaPlayer()
        mNextMediaPlayer!!.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        mNextMediaPlayer!!.audioSessionId = audioSessionId

        if(
            setDataSourceImpl(mNextMediaPlayer!!, path)
        ) {
            try {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "setNextDataSource: setNextMediaPlayer()", e);

                if (
                    mNextMediaPlayer != null
                ) {
                    mNextMediaPlayer!!.release()
                    mNextMediaPlayer = null
                }
            }
        }
    }

    override fun setCallbacks(
        callbacks: Playback.PlaybackCallbacks
    ) {
        this.callbacks = callbacks
    }

    override fun start(): Boolean {
        return try {
            mCurrentMediaPlayer.start()
            true
        } catch (e: IllegalStateException){
            false
        }
    }

    override fun stop() {
        mCurrentMediaPlayer.reset()
        mIsInitialized = false
    }

    override fun release() {
        stop()

        mCurrentMediaPlayer.release()
        mNextMediaPlayer?.release()
    }

    override fun pause(): Boolean {
        return try {
            mCurrentMediaPlayer.pause()
            true
        } catch (e: IllegalStateException){
            false
        }
    }

    override fun duration(): Long {
        return if (!mIsInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.duration.toLong()
        } catch (e: IllegalStateException) {
            -1
        }
    }

    override fun position(): Long {
        return if (!mIsInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.currentPosition.toLong()
        } catch (e: java.lang.IllegalStateException) {
            -1
        }
    }

    override fun seek(whereto: Long): Long {
        return try {
            mCurrentMediaPlayer.seekTo(whereto.toInt())
            whereto
        } catch (e: IllegalStateException){
            -1
        }
    }

    override fun setVolume(vol: Float): Boolean {
        return try {
            mCurrentMediaPlayer.setVolume(vol, vol)
            true
        } catch (e: IllegalStateException){
            false
        }
    }

    override fun setAudioSessionId(
        sessionId: Int
    ): Boolean {
        return try {
            mCurrentMediaPlayer.audioSessionId = sessionId
            true
        } catch (e: IllegalStateException){
            false
        }
    }

    override fun onError(
        mediaPlayer: MediaPlayer,
        what: Int,
        extra: Int
    ): Boolean {
        mIsInitialized = false
        mCurrentMediaPlayer.release()
        mCurrentMediaPlayer = MediaPlayer()
        mCurrentMediaPlayer.setWakeMode(

            context,
            PowerManager.PARTIAL_WAKE_LOCK
        )
        if (context != null) {
            Toast.makeText(
                context,
                context!!.resources.getString(
                    R.string.unplayable_file
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    override fun onCompletion(
        mediaPlayer: MediaPlayer
    ) {
        if(
            mediaPlayer == mCurrentMediaPlayer &&
                    mNextMediaPlayer != null
        ) {
            mIsInitialized = false
            mCurrentMediaPlayer.release()
            mCurrentMediaPlayer = mNextMediaPlayer as MediaPlayer

            mIsInitialized = true
            mNextMediaPlayer = null
            callbacks?.onMediaWentToNext()
        } else {
            callbacks?.onMediaEnded()
        }
    }
}