package com.example.elect.mediaplayer.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(indices = [Index(
    value = ["playlist_creator_id", "id"],
    unique = true)]
)
class SongEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "song_key")
    val songPrimaryKey: Long = 0L,
    @ColumnInfo(name = "playlist_creator_id")
    val playlistCreatorId: Long,
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
    val albumArtist: String?
) : Parcelable