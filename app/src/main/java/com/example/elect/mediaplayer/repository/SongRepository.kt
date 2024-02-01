package com.example.elect.mediaplayer.repository

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.elect.mediaplayer.Constants.baseMediaSongProjection
import com.example.elect.mediaplayer.Constants.baseProjection
import com.example.elect.mediaplayer.Constants.baseSongFolderProjection
import com.example.elect.mediaplayer.extensions.getInt
import com.example.elect.mediaplayer.extensions.getLong
import com.example.elect.mediaplayer.extensions.getString
import com.example.elect.mediaplayer.extensions.getStringOrNull
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.model.SongFolder
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import java.text.Collator
import java.util.ArrayList

interface SongRepository {
    fun song(cursor: Cursor?): Song
    fun songs(): List<Song>
    fun songs(cursor: Cursor?): List<Song>
    fun songFolders(): List<SongFolder>
    fun songFolders(cursor: Cursor?): List<SongFolder>

    suspend fun songs(songIds: List<Long>): List<Song>

    fun songFolder(folderId: Long): SongFolder

    fun mediaSongs(): List<Media>
    fun mediaSongs(cursor: Cursor?): List<Media>
}

class RealSongRepository(
    private val context: Context
    ): SongRepository {

    override fun song(cursor: Cursor?): Song {
        val song: Song =
            if (cursor != null && cursor.moveToFirst()) {
                getSongFromCursorImpl(cursor)
            }
            else {
                Song.emptySong
            }
        cursor?.close()
        return song
    }

    override fun songs(): List<Song> {

        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"

        return songs(makeSongCursor(selection, null))
    }

    override fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if(cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()


        val collator = Collator.getInstance()

        return when(PreferenceUtil.songSortOrder) {
            SortOrder.SongSortOrder.SONG_A_Z -> {
                songs.sortedWith{s1, s2 ->
                    collator.compare(s1.title, s2.title)
                }
            }
            SortOrder.SongSortOrder.SONG_Z_A -> {
                songs.sortedWith{s1, s2 ->
                    collator.compare(s2.title, s1.title)
                }
            }
            SortOrder.SongSortOrder.SONG_ALBUM -> {
                songs.sortedWith{s1, s2 ->
                    collator.compare(s1.albumName, s2.albumName)
                }
            }
            SortOrder.SongSortOrder.SONG_ARTIST -> {
                songs.sortedWith{s1, s2 ->
                    collator.compare(s1.artistName, s2.artistName)
                }
            }
            else -> songs
        }
    }

    private fun getSongFromCursorImpl(
        cursor: Cursor
    ): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(MediaStore.Audio.AudioColumns.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val folderId = cursor.getLong(MediaStore.Audio.AudioColumns.BUCKET_ID)
        val folderName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")

        return Song(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName ?: "",
            artistId,
            artistName ?: "",
            folderId,
            folderName ?: "",
            composer ?: "",
            albumArtist ?: ""
        )
    }

    @JvmOverloads
    fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.songSortOrder
    ): Cursor? {
        val selectionFinal = selection
        val selectionValuesFinal = selectionValues

        val uri = if(BuildUtil.isQPlus()){
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return try {
            context.contentResolver.query(
                uri,
                baseProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException) {
            return null
        }
    }

    override fun songFolders(): List<SongFolder> {
        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"
        var tmpList = songFolders(makeSongFolderCursor(selection, null))
        tmpList = tmpList.distinct()

        val songs = mediaSongs()

        tmpList.map { songFolder ->
            songs.map { media ->
                if(songFolder.id == media.folderId){
                    songFolder.medias += media
                }
            }
        }

        return tmpList
    }

    override fun songFolders(
        cursor: Cursor?
    ): List<SongFolder> {
        val songFolders = arrayListOf<SongFolder>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                songFolders.add(getSongFolderFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()

        val collator = Collator.getInstance()

        return when(PreferenceUtil.songFolderSortOrder) {
            SortOrder.SongFolderSortOrder.SONG_FOLDER_A_Z -> {
                songFolders.sortedWith{s1, s2 ->
                    collator.compare(s1.folderName, s2.folderName)
                }
            }
            SortOrder.SongFolderSortOrder.SONG_FOLDER_Z_A -> {
                songFolders.sortedWith{s1, s2 ->
                    collator.compare(s2.folderName, s1.folderName)
                }
            }
            else -> songFolders
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun songFolder(folderId: Long): SongFolder {
        val cursor = makeSongCursor(
            MediaStore.Audio.AudioColumns.BUCKET_ID + "=?",
            arrayOf(folderId.toString()),
            getSongLoaderSortOrder()
        )
        val medias = mediaSongs(cursor)
        val folderName = medias.firstOrNull()?.folderName
        val folder = folderName?.let { SongFolder(folderId, it, medias) }
        return if(folder != null){
            sortSongFolderMedias(folder)
        } else {
            SongFolder(
                -1,
                "",
                ArrayList()
            )
        }
    }

    private fun getSongLoaderSortOrder(): String {
        var folderSortOrder = PreferenceUtil.songFolderDetailsSortOrder
        if (folderSortOrder == SortOrder.SongFolderDetailsSortOrder.SONG_A_Z)
            folderSortOrder = SortOrder.SongFolderDetailsSortOrder.SONG_A_Z
        return folderSortOrder + ", " +
                PreferenceUtil.songFolderDetailsSortOrder
    }

    private fun sortSongFolderMedias(songFolder: SongFolder): SongFolder {
        val collator = Collator.getInstance()
        val medias = when (PreferenceUtil.songFolderDetailsSortOrder) {
            SortOrder.SongFolderDetailsSortOrder.SONG_A_Z -> {
                songFolder.medias.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }
            SortOrder.SongFolderDetailsSortOrder.SONG_Z_A -> {
                songFolder.medias.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }
            else -> throw IllegalArgumentException("invalid ${PreferenceUtil.songFolderDetailsSortOrder}")
        }
        return songFolder.copy(medias = medias)
    }

    private fun getSongFolderFromCursorImpl(
        cursor: Cursor
    ): SongFolder {


        val folderId = cursor.getLong(MediaStore.Audio.AudioColumns.BUCKET_ID)
        val folderName = cursor.getString(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)

        return SongFolder(
            folderId,
            folderName,
            arrayListOf()
        )
    }

    fun makeSongFolderCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.songFolderSortOrder
    ): Cursor?{
        val selectionFinal = selection
        val selectionValuesFinal = selectionValues

        val uri = if(BuildUtil.isQPlus()){
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return try {
            context.contentResolver.query(
                uri,
                baseSongFolderProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException){
            return null
        }
    }



    override suspend fun songs(songIds: List<Long>): List<Song> {
        val list = mutableListOf<Song>()
        songIds.map { songId ->
            list += song(
                makeSongCursor(
                    MediaStore.Audio.AudioColumns._ID + "=?",
                    arrayOf(songId.toString())
                )
            )
        }

        return list
    }

    override fun mediaSongs(): List<Media> {
        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"

        return mediaSongs(makeMediaSongCursor(selection, null))
    }

    override fun mediaSongs(cursor: Cursor?): List<Media> {
        val medias = arrayListOf<Media>()
        if(cursor != null && cursor.moveToFirst()) {
            do {
                medias.add(getMediaSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()


        val collator = Collator.getInstance()

        return when(PreferenceUtil.songSortOrder) {
            SortOrder.SongSortOrder.SONG_A_Z -> {
                medias.sortedWith{s1, s2 ->
                    collator.compare(s1.title, s2.title)
                }
            }
            SortOrder.SongSortOrder.SONG_Z_A -> {
                medias.sortedWith{s1, s2 ->
                    collator.compare(s2.title, s1.title)
                }
            }
            SortOrder.SongSortOrder.SONG_ALBUM -> {
                medias.sortedWith{s1, s2 ->
                    collator.compare(s1.albumName, s2.albumName)
                }
            }
            SortOrder.SongSortOrder.SONG_ARTIST -> {
                medias.sortedWith{s1, s2 ->
                    collator.compare(s1.artistName, s2.artistName)
                }
            }
            else -> medias
        }
    }

    private fun getMediaSongFromCursorImpl(
        cursor: Cursor
    ): Media {
        val id = cursor.getLong(BaseColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(MediaStore.Audio.AudioColumns.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val folderId = cursor.getLong(MediaStore.Audio.AudioColumns.BUCKET_ID)
        val folderName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)

        val songId = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")

        return Media(
            id,
            title,
            year,
            duration,
            data,
            dateModified,
            folderId,
            folderName ?: "",

            songId,
            trackNumber,
            albumId,
            albumName ?: "",
            artistId,
            artistName ?: "",
            composer ?: "",
            albumArtist ?: "",

            videoId = -1,
            size = "",

            isSongOrVideo = 1
        )
    }

    @JvmOverloads
    fun makeMediaSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.songSortOrder
    ): Cursor? {
        val selectionFinal = selection
        val selectionValuesFinal = selectionValues

        val uri = if(BuildUtil.isQPlus()){
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return try {
            context.contentResolver.query(
                uri,
                baseMediaSongProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException) {
            return null
        }
    }
}