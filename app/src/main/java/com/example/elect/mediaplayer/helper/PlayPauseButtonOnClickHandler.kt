package com.example.elect.mediaplayer.helper

import android.view.View
import android.widget.Toast

class PlayPauseButtonOnClickHandler : View.OnClickListener {
    override fun onClick(v: View) {
        if (PlayerRemote.isPlaying) {
            PlayerRemote.pauseMedia()
        } else {
            PlayerRemote.resumeMedia()
        }
    }
}
