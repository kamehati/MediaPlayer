package com.example.elect.mediaplayer

import android.provider.BaseColumns
import android.provider.MediaStore

object Constants {


    @Suppress("Deprecation")
    val baseProjection = arrayOf(
        BaseColumns._ID,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.TRACK,
        MediaStore.Audio.AudioColumns.YEAR,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DATE_MODIFIED,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.BUCKET_ID,
        MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.COMPOSER,
        ALBUM_ARTIST,
        IS_FAVORITE
    )

    @Suppress("Deprecation")
    val baseVideoProjection = arrayOf(
        BaseColumns._ID,
        MediaStore.Video.VideoColumns.TITLE,
        MediaStore.Video.VideoColumns.YEAR,
        MediaStore.Video.VideoColumns.DURATION,
        MediaStore.Video.VideoColumns.DATA,
        MediaStore.Video.VideoColumns.DATE_MODIFIED,
        MediaStore.Video.VideoColumns.BUCKET_ID,
        MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Video.VideoColumns.SIZE
    )

    @Suppress("Deprecation")
    val baseMediaSongProjection = arrayOf(
        BaseColumns._ID,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.YEAR,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DATE_MODIFIED,
        MediaStore.Audio.AudioColumns.BUCKET_ID,
        MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,

        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.TRACK,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.COMPOSER,
        MediaStore.Audio.AudioColumns.SIZE,
        ALBUM_ARTIST,
    )

    @Suppress("Deprecation")
    val baseMediaVideoProjection = arrayOf(
        BaseColumns._ID,
        MediaStore.Video.VideoColumns.TITLE,
        MediaStore.Video.VideoColumns.YEAR,
        MediaStore.Video.VideoColumns.DURATION,
        MediaStore.Video.VideoColumns.DATA,
        MediaStore.Video.VideoColumns.DATE_MODIFIED,
        MediaStore.Video.VideoColumns.BUCKET_ID,
        MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,

        MediaStore.Video.VideoColumns._ID,
        MediaStore.Video.VideoColumns.SIZE,

    )

    @Suppress("Deprecation")
    val baseVideoFolderProjection = arrayOf(
        MediaStore.Video.VideoColumns.BUCKET_ID,
        MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME
    )

    @Suppress("Deprecation")
    val baseSongFolderProjection = arrayOf(
        MediaStore.Audio.AudioColumns.BUCKET_ID,
        MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
    )

    @Suppress("Deprecation")
    val baseAlbumProjection = arrayOf(
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ALBUM
    )

    @Suppress("Deprecation")
    val baseArtistProjection = arrayOf(
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        MediaStore.Audio.AudioColumns.ARTIST
    )
}
const val LAST_TAB = "last_tab"
const val REMEMBER_LAST_TAB = "remember_last_tab"

const val VIDEO_FOLDER_SORT_ORDER = "video_folder_sort_order"
const val VIDEO_SORT_ORDER = "video_sort_order"

const val SONG_SORT_ORDER = "song_sort"
const val SONG_FOLDER_SORT_ORDER = "song_folder_sort_order"
const val ALBUM_SORT_ORDER = "album_sort_order"
const val ARTIST_SORT_ORDER = "artist_sort_order"
const val FAVORITE_SORT_ORDER = "favorite_sort_order"
const val PLAYLIST_SORT_ORDER = "playlist_sort_order"

const val ALBUM_DETAILS_SORT_ORDER = "album_details_sort_order"
const val ARTIST_DETAILS_SORT_ORDER = "artist_details_sort_order"
const val SONG_FOLDER_DETAILS_SORT_ORDER = "song_folder_details_sort_order"
const val VIDEO_FOLDER_DETAILS_SORT_ORDER = "video_folder_details_sort_order"

const val VIDEO_FOLDER_GRID_SIZE = "video_folder_grid_size"
const val VIDEO_GRID_SIZE = "video_grid_size"

const val SONG_GRID_SIZE = "song_grid"
const val SONG_FOLDER_GRID_SIZE = "song_folder_grid_size"
const val ALBUM_GRID_SIZE = "album_grid_size"
const val ARTIST_GRID_SIZE = "artist_grid_size"
const val FAVORITE_GRID_SIZE = "favorite_grid_size"
const val PLAYLIST_GRID_SIZE = "playlist_grid_size"

const val EXTRA_MEDIAS = "extra_medias"
const val EXTRA_PLAYLISTS = "extra_playlists"


const val ALBUM_ARTIST = "album_artist"


const val EMPTY_SONG_ID = "empty_song_id"
const val EMPTY_TRACK = "empty_track"
const val EMPTY_ALBUM_ID = "empty_album_id"
const val EMPTY_ALBUM_NAME = "empty_album_name"
const val EMPTY_ARTIST_ID = "empty_artist_name"
const val EMPTY_ARTIST_NAME = "empty_artist_name"
const val EMPTY_COMPOSER_NAME = "empty_composer_name"

const val EMPTY_VIDEO_ID = "empty_video_id"
const val EXTRA_PLAYLIST = "extra_playlist"
const val EXTRA_PLAYLIST_ID = "extra_playlist_id"

const val EMPTY_IS_S_OR_V = "empty_is_s_or_v"

const val IS_FAVORITE = "is_favorite"

const val EXTRA_ALBUM_ID = "extra_album_id"
const val EXTRA_VIDEO_FOLDER_ID = "extra_video_folder_id"
const val EXTRA_ARTIST_ID = "extra_artist_id"
const val EXTRA_SONG_FOLDER_ID = "extra_song_folder_id"

const val APPBAR_MODE = "appbar_mode"
const val IS_SONG_OR_VIDEO = "is_song_or_video"
const val BRIGHTNESS = "brightness"
const val MEDIA_LOUDNESS = "media_loudness"
const val EXO_LOUDNESS = "exo_loudness"

const val VIEWPAGER2_TAB_POSITION = "viewpager2_tab_position"

const val SAVED_POSITION = "saved_position"
const val SAVED_POSITION_IN_TRACK = "saved_position_in_track"

const val VIDEO_PROGRESS_MILLIS = "video_progress_millis"
const val NOW_PLAYING_FRAGMENT = "now_playing_fragment"