package com.example.elect.mediaplayer.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.elect.mediaplayer.glide.audiocover.AudioFileCover
import com.example.elect.mediaplayer.glide.audiocover.AudioFileCoverLoader
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteTranscoder
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteWrapper
import java.io.InputStream


@GlideModule
class AppGlideModule : AppGlideModule() {

    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry
    ) {

        registry.prepend(
            AudioFileCover::class.java,
            InputStream::class.java,
            AudioFileCoverLoader.Factory(context)
        )


        registry.register(
            Bitmap::class.java,
            BitmapPaletteWrapper::class.java,
            BitmapPaletteTranscoder()
        )
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}