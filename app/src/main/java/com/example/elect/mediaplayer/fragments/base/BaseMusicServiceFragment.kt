package com.example.elect.mediaplayer.fragments.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.elect.mediaplayer.activity.base.BaseMusicServiceActivity
import com.example.elect.mediaplayer.interfaces.IMusicServiceEventListener

abstract class BaseMusicServiceFragment(
    @LayoutRes layout: Int
) : Fragment(layout),
    IMusicServiceEventListener {

    var serviceActivity: BaseMusicServiceActivity? = null
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            serviceActivity =
                context as BaseMusicServiceActivity?
        } catch (e: ClassCastException) {
            throw RuntimeException(
                context.javaClass.simpleName +
                        " must be an instance of " +
                        BaseMusicServiceActivity::class.java.simpleName
            )
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        serviceActivity?.addMusicServiceEventListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        serviceActivity?.removeMusicServiceEventListener(this)
    }

    override fun onFavoriteStateChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
    override fun onMediaStoreChanged() {}
}