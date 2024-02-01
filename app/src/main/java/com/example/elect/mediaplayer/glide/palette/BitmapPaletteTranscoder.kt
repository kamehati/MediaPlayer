package com.example.elect.mediaplayer.glide.palette

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder


class BitmapPaletteTranscoder :
    ResourceTranscoder<Bitmap, BitmapPaletteWrapper> {

    override fun transcode(
        toTranscode: Resource<Bitmap>,
        options: Options
    ): Resource<BitmapPaletteWrapper>{
        val bitmap = toTranscode.get()
        val palette = Palette.from(bitmap).clearFilters().generate()
        val bitmapPaletteWrapper = BitmapPaletteWrapper(bitmap, palette)

        return BitmapPaletteResource(bitmapPaletteWrapper)
    }
}