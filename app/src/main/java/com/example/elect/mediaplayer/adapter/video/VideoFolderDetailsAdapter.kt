package com.example.elect.mediaplayer.adapter.video

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Video

class VideoFolderDetailsAdapter(
    override val activity: FragmentActivity,
    override var dataSet: MutableList<Media>,
    ICabHolder: ICabHolder?,
    override val fragment: Fragment
): VideoAdapter(
    activity, dataSet, ICabHolder, fragment
) {
}