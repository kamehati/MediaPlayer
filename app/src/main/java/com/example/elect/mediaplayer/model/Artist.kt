package com.example.elect.mediaplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: Long,
    val artistName: String,
    var medias: List<Media>

) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Artist

        if (id != other.id) return false
        if (artistName != other.artistName) return false
        if (medias != other.medias) return false


        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + artistName.hashCode()
        result = 31 * result + medias.hashCode()


        return result
    }

    fun safeGetFirstMedia(): Media {
        return medias.firstOrNull() ?: Media.emptyMedia
    }
}