package com.example.elect.mediaplayer.glide.palette

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

class BitmapPaletteWrapper(
    bitmap: Bitmap,
    palette: Palette
) {
    private val mBitmap = bitmap
    private val mPalette = palette

    fun getBitmap(): Bitmap{return mBitmap}

    fun getPalette(): Palette{return mPalette}
}