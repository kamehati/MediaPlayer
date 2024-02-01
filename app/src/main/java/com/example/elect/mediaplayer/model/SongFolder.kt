package com.example.elect.mediaplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongFolder(
    val id: Long,
    val folderName: String,
    var medias: List<Media>

) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as SongFolder

        if (id != other.id) return false
        if (folderName != other.folderName) return false
        if (medias != other.medias) return false


        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + folderName.hashCode()
        result = 31 * result + medias.hashCode()


        return result
    }



    fun safeGetFirstMediaSong(): Media { return medias.firstOrNull() ?: Media.emptyMedia }
}
