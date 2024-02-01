package com.example.elect.mediaplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
open class Video(
    open val id: Long,
    open var title: String,
    open val year: Int,
    open val duration: Long,
    open val data: String,
    open val dateModified: Long,
    open val folderId: Long,
    open val folderName: String,
    open val size: String,
    open var artUri: Uri
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Video

        if(id != other.id) return false
        if (title != other.title) return false
        if (year != other.year) return false
        if (duration != other.duration) return false
        if (data != other.data) return false
        if (dateModified != other.dateModified) return false
        if (folderId != other.folderId) return false
        if (folderName != other.folderName) return false
        if (size != other.size) return false
        if (artUri != other.artUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()

        result = 31 * result + title.hashCode()
        result = 31 * result + year
        result = 31 * result + duration.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + folderId.hashCode()
        result = 31 * result + folderName.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + artUri.hashCode()

        return result
    }

    companion object {
        @JvmStatic
        val emptyVideo = Video(
            id = -1,
            title = "",
            year = -1,
            duration = -1,
            data = "",
            dateModified = -1,
            folderId = -1,
            folderName = "",
            size = "",
            artUri = Uri.fromFile(File(""))
        )
    }
}
