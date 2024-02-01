package com.example.elect.mediaplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
open class Media(
    open val id: Long,
    open val title: String,
    open val year: Int,
    open val duration: Long,
    open val data: String,
    open val dateModified: Long,
    open val folderId: Long,
    open val folderName: String,

    open val songId: Long,
    open val trackNumber: Int,
    open val albumId: Long,
    open val albumName: String,
    open val artistId: Long,
    open val artistName: String,
    open val composer: String?,
    open val albumArtist: String?,

    open val videoId: Long,
    open val size: String,

    open val isSongOrVideo: Int
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Media

        if (id != other.id) return false
        if (title != other.title) return false
        if (year != other.year) return false
        if (duration != other.duration) return false
        if (data != other.data) return false
        if (dateModified != other.dateModified) return false
        if (folderId != other.folderId) return false
        if (folderName != other.folderName) return false

        if (songId != other.songId) return false
        if (trackNumber != other.trackNumber) return false
        if (albumId != other.albumId) return false
        if (albumName != other.albumName) return false
        if (artistId != other.artistId) return false
        if (artistName != other.artistName) return false
        if (composer != other.composer) return false
        if (albumArtist != other.albumArtist) return false

        if (videoId != other.videoId) return false
        if (size != other.size) return false

        if (isSongOrVideo != other.isSongOrVideo) return false

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

        result = 31 * result + trackNumber
        result = 31 * result + albumId.hashCode()
        result = 31 * result + albumName.hashCode()
        result = 31 * result + artistId.hashCode()
        result = 31 * result + artistName.hashCode()
        result = 31 * result + (composer?.hashCode() ?: 0)
        result = 31 * result + (albumArtist?.hashCode() ?: 0)

        result = 31 * result + videoId.hashCode()
        result = 31 * result + size.hashCode()

        result = 31 * result + isSongOrVideo.hashCode()

        return result
    }

    companion object {
        @JvmStatic
        val emptyMedia = Media(
            id = -1,
            title = "",
            year = -1,
            duration = -1,
            data = "",
            dateModified = -1,
            folderId = -1,
            folderName = "",

            songId = -1,
            trackNumber = -1,
            albumId = -1,
            albumName = "",
            artistId = -1,
            artistName = "",
            composer = "",
            albumArtist = "",

            videoId = -1,
            size = "",

            isSongOrVideo = -1
        )
    }
}