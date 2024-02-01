package com.example.elect.mediaplayer.glide.palette

import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget

open class BitmapPaletteTarget(view: ImageView)
    : ImageViewTarget<BitmapPaletteWrapper>(view) {

    fun bitmapPaletteTarget(view: ImageView){super.view}


    override fun setResource(
        resource: BitmapPaletteWrapper?
    ) {
        if(resource != null){
            view.setImageBitmap(
                resource.getBitmap()
            )
        }
    }
}