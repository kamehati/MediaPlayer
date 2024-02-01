package com.example.elect.mediaplayer.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


abstract class BaseActivityFragment (
    @LayoutRes layout: Int
) : BaseMusicServiceFragment(layout) {


    val libraryViewModel: LibraryViewModel by sharedViewModel()

    val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
    }
}