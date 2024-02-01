package com.example.elect.mediaplayer.glide.palette

import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.util.Util


class BitmapPaletteResource(
    bitmapPaletteWrapper: BitmapPaletteWrapper
) : Resource<BitmapPaletteWrapper> {

    private val mBitmapPaletteWrapper = bitmapPaletteWrapper

    override fun get(): BitmapPaletteWrapper {
        return mBitmapPaletteWrapper
    }

    override fun getResourceClass(): Class<BitmapPaletteWrapper> {
        return BitmapPaletteWrapper::class.java
    }

    override fun getSize(): Int {
        return Util.getBitmapByteSize(mBitmapPaletteWrapper.getBitmap())
    }

    override fun recycle() {
        mBitmapPaletteWrapper.getBitmap().recycle()
    }
}