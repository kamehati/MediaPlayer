package com.example.elect.mediaplayer.db

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
class MediaFavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "primary_id")
    val mediaPrimaryKey: Long = 0L,
    @ColumnInfo(name = "media_id")
    val id: Long,
    val title: String,
    val year: Int,
    val duration: Long,
    val data: String,
    val dateModified: Long,
    val folderId: Long,
    val folderName: String,

    val songId: Long,
    val trackNumber: Int,
    val albumId: Long,
    val albumName: String,
    val artistId: Long,
    val artistName: String,
    val composer: String?,
    val albumArtist: String?,

    val videoId: Long,
    val size: String,

    val isSongOrVideo: Int,
    @ColumnInfo(name = "media_is_favorite")
    val isFavorite: Boolean
) : Parcelable