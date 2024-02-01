package com.example.elect.mediaplayer.glide.audiocover

import androidx.annotation.Nullable


class AudioFileCover(val filePath: String) {

    override fun hashCode(): Int {
        return filePath.hashCode()
    }

    override fun equals(@Nullable other: Any?): Boolean {

        if(other is AudioFileCover){
            return other.filePath == filePath
        }
        return false
    }
}