package com.example.elect.mediaplayer.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.example.elect.mediaplayer.Constants
import com.example.elect.mediaplayer.Constants.baseAlbumProjection
import com.example.elect.mediaplayer.Constants.baseProjection
import com.example.elect.mediaplayer.extensions.getLong
import com.example.elect.mediaplayer.extensions.getString
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import java.text.Collator
import java.util.ArrayList

interface AlbumRepository {

    fun albumBuckets(): List<Album>

    fun albumBuckets(cursor: Cursor?): List<Album>

    fun album(albumId: Long): Album
}

class RealAlbumRepository(
    private val context: Context,
    private val songRepository: SongRepository
): AlbumRepository {

    override fun albumBuckets(): List<Album> {
        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"
        var tempList = albumBuckets(
            makeAlbumCursor(
                selection,
                null,
                sortOrder = getSongLoaderSortOrder()
            )
        )
        tempList = tempList.distinct()

        val medias = songRepository.mediaSongs()


        tempList.map { album ->

            medias.map { media ->
                if(album.id == media.albumId){
                    album.medias += media
                }
            }
        }
        return tempList
    }

    override fun albumBuckets(
        cursor: Cursor?
    ): List<Album> {
        val albums = arrayListOf<Album>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                albums.add(getAlbumFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()

        val collator = Collator.getInstance()

        return when(PreferenceUtil.albumSortOrder) {
            SortOrder.AlbumSortOrder.ALBUM_A_Z -> {
                albums.sortedWith{s1, s2 ->
                    collator.compare(s1.albumName, s2.albumName)
                }
            }
            SortOrder.AlbumSortOrder.ALBUM_Z_A -> {
                albums.sortedWith{s1, s2 ->
                    collator.compare(s2.albumName, s1.albumName)
                }
            }
            SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS -> {
                albums.sortedByDescending { it.mediaCount }
            }
            else -> albums
        }
    }

    private fun getAlbumFromCursorImpl(
        cursor: Cursor
    ): Album{

        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getString(MediaStore.Audio.AudioColumns.ALBUM)

        return Album(
            albumId,
            albumName,
            arrayListOf()
        )
    }

    fun makeAlbumCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.albumSortOrder
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
                baseAlbumProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException){
            return null
        }
    }

    override fun album(albumId: Long): Album {

        val cursor = makeSongCursor(
            MediaStore.Audio.AudioColumns.ALBUM_ID + "=?",
            arrayOf(albumId.toString()),
            getSongLoaderSortOrder()
        )
        val medias = songRepository.mediaSongs(cursor)
        val albumName = medias.firstOrNull()?.albumName
        val album = albumName?.let { Album(albumId, it, medias) }
        return if(album != null){
            sortAlbumSongs(album)
        } else {
            Album(
                -1,
                "",
                ArrayList()
            )
        }
    }

    private fun sortAlbumSongs(album: Album): Album {
        val collator = Collator.getInstance()
        val medias = when (PreferenceUtil.albumDetailsSortOrder) {
            SortOrder.AlbumDetailsSortOrder.SONG_A_Z -> {
                album.medias.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }
            SortOrder.AlbumDetailsSortOrder.SONG_Z_A -> {
                album.medias.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }
            else -> throw IllegalArgumentException("invalid ${PreferenceUtil.albumDetailsSortOrder}")
        }
        return album.copy(medias = medias)
    }

    @JvmOverloads
    fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.albumDetailsSortOrder
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

    private fun getSongLoaderSortOrder(): String {
        var albumSortOrder = PreferenceUtil.albumSortOrder
        if (albumSortOrder == SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS)
            albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z
        return albumSortOrder + ", " +
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
    }
}