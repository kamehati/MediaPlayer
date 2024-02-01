package com.example.elect.mediaplayer.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.example.elect.mediaplayer.Constants
import com.example.elect.mediaplayer.extensions.getLong
import com.example.elect.mediaplayer.extensions.getString
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.model.Artist
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import java.text.Collator
import java.util.ArrayList

interface ArtistRepository {

    fun artistBuckets(): List<Artist>

    fun artistBuckets(cursor: Cursor?): List<Artist>

    fun artist(artistId: Long): Artist
}

class RealArtistRepository(
    private val context: Context,
    private val songRepository: SongRepository
): ArtistRepository {

    override fun artistBuckets(): List<Artist> {
        var tempList = artistBuckets(makeArtistCursor(null, null))
        tempList = tempList.distinct()

        val medias = songRepository.mediaSongs()


        tempList.map { artist ->

            medias.map { media ->
                if(artist.id == media.artistId){
                    artist.medias += media
                }
            }
        }
        return tempList
    }

    override fun artistBuckets(
        cursor: Cursor?
    ): List<Artist> {
        val artists = arrayListOf<Artist>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                artists.add(getArtistFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()

        val collator = Collator.getInstance()

        return when(PreferenceUtil.artistSortOrder) {
            SortOrder.ArtistSortOrder.ARTIST_A_Z -> {
                artists.sortedWith{s1, s2 ->
                    collator.compare(s1.artistName, s2.artistName)
                }
            }
            SortOrder.ArtistSortOrder.ARTIST_Z_A -> {
                artists.sortedWith{s1, s2 ->
                    collator.compare(s2.artistName, s1.artistName)
                }
            }
            else -> artists
        }
    }

    override fun artist(artistId: Long): Artist {
        val cursor = makeSongCursor(
            MediaStore.Audio.AudioColumns.ARTIST_ID + "=?",
            arrayOf(artistId.toString()),
            getSongLoaderSortOrder()
        )
        val medias = songRepository.mediaSongs(cursor)
        val artistName = medias.firstOrNull()?.artistName
        val artist = artistName?.let { Artist(artistId, artistName, medias) }
        return if(artist != null){
            sortArtistSongs(artist)
        } else {
            Artist(
                -1,
                "",
                ArrayList()
            )
        }
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
                Constants.baseProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException) {
            return null
        }
    }

    private fun getSongLoaderSortOrder(): String {
        val artistSortOrder = PreferenceUtil.artistSortOrder
        return artistSortOrder + ", " +
                PreferenceUtil.artistSortOrder
    }

    private fun sortArtistSongs(artist: Artist): Artist {
        val collator = Collator.getInstance()
        val medias = when (PreferenceUtil.artistDetailsSortOrder) {
            SortOrder.ArtistDetailsSortOrder.SONG_A_Z -> {
                artist.medias.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }
            SortOrder.ArtistDetailsSortOrder.SONG_Z_A -> {
                artist.medias.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }
            else -> throw IllegalArgumentException("invalid ${PreferenceUtil.artistDetailsSortOrder}")
        }
        return artist.copy(medias = medias)
    }

    private fun getArtistFromCursorImpl(
        cursor: Cursor
    ): Artist{

        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getString(MediaStore.Audio.AudioColumns.ARTIST)

        return Artist(
            artistId,
            artistName,
            arrayListOf()
        )
    }

    fun makeArtistCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.artistSortOrder
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
                Constants.baseArtistProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException){
            return null
        }
    }
}