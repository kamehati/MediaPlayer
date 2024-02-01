package com.example.elect.mediaplayer.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "primary_id")
    val songPrimaryKey: Long = 0L,
    @ColumnInfo(name = "song_id")
    val id: Long,
    val title: String,
    @ColumnInfo
    val trackNumber: Int,
    val year: Int,
    val duration: Long,
    val data: String,
    @ColumnInfo
    val dateModified: Long,
    @ColumnInfo
    val albumId: Long,
    @ColumnInfo
    val albumName: String,
    @ColumnInfo
    val artistId: Long,
    @ColumnInfo
    val artistName: String,
    @ColumnInfo
    val folderId: Long,
    @ColumnInfo
    val folderName: String,
    val composer: String?,
    @ColumnInfo
    val albumArtist: String?,
    @ColumnInfo(name = "song_is_favorite")
    val isFavorite: Boolean
) : Parcelable