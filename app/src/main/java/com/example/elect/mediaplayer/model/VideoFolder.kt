package com.example.elect.mediaplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoFolder(
    val id: Long,
    val folderName: String,
    var videos: List<Media>

) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as VideoFolder

        if (id != other.id) return false
        if (folderName != other.folderName) return false
        if (videos != other.videos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + folderName.hashCode()
        result = 31 * result + videos.hashCode()

        return result
    }

    fun safeGetFirstVideo(): Media {
        return videos.firstOrNull() ?: Media.emptyMedia

    }
}
