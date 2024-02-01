package com.example.elect.mediaplayer.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.db.PlaylistEntity
import com.example.elect.mediaplayer.db.toSongEntity
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.model.Video
import com.example.elect.mediaplayer.repository.RealRoomRepository
import com.example.elect.mediaplayer.repository.Repository
import com.example.elect.mediaplayer.service.MusicService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object MusicUtil : KoinComponent {
    fun getDateModifiedString(date: Long): String{
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val pattern = "dd/MM/yyyy hh:mm:ss"
        val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)

        return formatter.format(calendar.time)
    }

    fun getReadableDurationString(
        songDurationMillis: Long
    ): String {
        var minutes = songDurationMillis / 1000 / 60
        val seconds = songDurationMillis / 1000 % 60

        return if (minutes < 60) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
            )
        } else {
            val hours = minutes / 60
            minutes %= 60
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        }
    }

    fun getYearString(year: Int): String{
        return if(year > 0) year.toString() else "-"
    }

    @JvmStatic
    fun getMediaStoreAlbumCoverUri(albumId: Long): Uri {
        val sArtworkUri = Uri.parse(
            "content://media/external/audio/albumart")
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    fun getSectionName(mediaTitle: String?): String{
        var musicMediaTitle = mediaTitle

        return try {
            if(TextUtils.isEmpty(musicMediaTitle)){
                return "-"
            }
            musicMediaTitle = mediaTitle!!
                .trim{ it <= ' ' }
                .lowercase()

            if(musicMediaTitle.startsWith("the ")){
                musicMediaTitle = musicMediaTitle.substring(4)
            }
            else if(musicMediaTitle.startsWith("a ")){
                musicMediaTitle = musicMediaTitle.substring(2)
            }

            if(musicMediaTitle.isEmpty()){
                ""
            }
            else musicMediaTitle.substring(0, 1).uppercase()
        } catch (e: Exception){
            ""
        }
    }

    fun getPlaylistInfoString(
        context: Context,
        medias: List<Media>
    ): String {
        val duration = getTotalDuration(medias)
        return buildInfoString(
            getSongCountString(context, medias.size),
            getReadableDurationString(duration)
        )
    }

    fun getTotalDuration(medias: List<Media>): Long {
        var duration: Long = 0
        for (i in medias.indices) {
            duration += medias[i].duration
        }
        return duration
    }

    fun getTotalVideoDuration(videos: List<Video>): Long {
        var duration: Long = 0
        for (i in videos.indices) {
            duration += videos[i].duration
        }
        return duration
    }

    fun getTotalMediaDuration(medias: List<Media>): Long {
        var duration: Long = 0
        for (i in medias.indices) {
            duration += medias[i].duration
        }
        return duration
    }

    fun buildInfoString(
        string1: String?,
        string2: String?
    ): String {
        if (string1.isNullOrEmpty()) {
            return if (string2.isNullOrEmpty()) ""
            else string2
        }
        return if (string2.isNullOrEmpty())
            if (string1.isNullOrEmpty()) ""
            else string1
        else "$string1  •  $string2"
    }

    fun getSongCountString(
        context: Context,
        songCount: Int
    ): String {
        val songString =
                context.resources.getString(R.string.song)
        return "$songCount $songString"
    }

    fun getMediaFileUri(media: Media): Uri {
        return when(media.isSongOrVideo){
            1 -> {
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    media.id
                )
            }
            2 -> {
                ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    media.id
                )
            }
            else -> {
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    media.id
                )
            }
        }
    }

    val repository = get<Repository>()
    fun toggleFavorite(context: Context, media: Media) {

    }

    fun isFavorite(media: Media): Boolean{
        return RealRoomRepository(get(), get()).isMediaFavorite(media.id)
    }
}