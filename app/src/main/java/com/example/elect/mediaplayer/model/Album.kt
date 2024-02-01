package com.example.elect.mediaplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: Long,
    val albumName: String,
    var medias: List<Media>

) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Album

        if (id != other.id) return false
        if (albumName != other.albumName) return false
        if (medias != other.medias) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + albumName.hashCode()
        result = 31 * result + medias.hashCode()


        return result
    }



    val mediaCount: Int
        get() = medias.size


    fun safeGetFirstMediaSong(): Media {
        return medias.firstOrNull() ?: Media.emptyMedia
    }
}