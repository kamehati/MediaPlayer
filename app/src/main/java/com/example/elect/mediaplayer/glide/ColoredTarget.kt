package com.example.elect.mediaplayer.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.transition.Transition
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteTarget
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteWrapper

abstract class ColoredTarget(view: ImageView)
    : BitmapPaletteTarget(view) {

    abstract fun onColorReady()


    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
    }


    override fun onResourceReady(
        resource: BitmapPaletteWrapper,
        transition: Transition<in BitmapPaletteWrapper>?
    ) {
        super.onResourceReady(resource, transition)

        onColorReady()
    }
}