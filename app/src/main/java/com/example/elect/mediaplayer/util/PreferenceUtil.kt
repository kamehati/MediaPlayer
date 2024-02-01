package com.example.elect.mediaplayer.util

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.elect.mediaplayer.*
import com.example.elect.mediaplayer.activity.Category
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.extensions.getStringOrDefault
import com.example.elect.mediaplayer.helper.SortOrder

object PreferenceUtil {

    private val sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(App.getContext())

    var lastTab: Int
    get() = sharedPreferences.getInt(
        LAST_TAB,
        Category.Videos.number
    )
    set(value) = sharedPreferences.edit{
        putInt(LAST_TAB,value)
    }

    var rememberLastTab: Boolean
    get() = sharedPreferences.getBoolean(
        REMEMBER_LAST_TAB,

        false
    )
    set(value) = sharedPreferences.edit{
        putBoolean(REMEMBER_LAST_TAB, value)
    }

    var videoFolderSortOrder
        get() = sharedPreferences.getStringOrDefault(
            VIDEO_FOLDER_SORT_ORDER,
            SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(VIDEO_FOLDER_SORT_ORDER, value)
        }

    var videoSortOrder
        get() = sharedPreferences.getStringOrDefault(
            VIDEO_SORT_ORDER,
            SortOrder.VideoSortOrder.VIDEO_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(VIDEO_SORT_ORDER, value)
        }


    var songSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_SORT_ORDER,
            SortOrder.SongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(SONG_SORT_ORDER, value)
        }

    var songFolderSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_FOLDER_SORT_ORDER,
            SortOrder.SongFolderSortOrder.SONG_FOLDER_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(SONG_FOLDER_SORT_ORDER, value)
        }

    var albumSortOrder
    get() = sharedPreferences.getStringOrDefault(
        ALBUM_SORT_ORDER,
        SortOrder.AlbumSortOrder.ALBUM_A_Z
    )
    set(value) = sharedPreferences.edit {
        putString(
            ALBUM_SORT_ORDER,
            value
        )
    }

    var artistSortOrder
    get() = sharedPreferences.getStringOrDefault(
        ARTIST_SORT_ORDER,
        SortOrder.ArtistSortOrder.ARTIST_A_Z
    )
    set(value) = sharedPreferences.edit {
        putString(ARTIST_SORT_ORDER, value)
    }

    var favoriteSortOrder
        get() = sharedPreferences.getStringOrDefault(
            FAVORITE_SORT_ORDER,
            SortOrder.FavoriteSortOrder.FAVORITE_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(FAVORITE_SORT_ORDER, value)
        }

    var playlistSortOrder
        get() = sharedPreferences.getStringOrDefault(
            PLAYLIST_SORT_ORDER,
            SortOrder.PlaylistSortOrder.PLAYLIST_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(PLAYLIST_SORT_ORDER, value)
        }

    var albumDetailsSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_DETAILS_SORT_ORDER,
            SortOrder.AlbumDetailsSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ALBUM_DETAILS_SORT_ORDER, value)
        }

    var artistDetailsSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_DETAILS_SORT_ORDER,
            SortOrder.ArtistDetailsSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ARTIST_DETAILS_SORT_ORDER, value)
        }

    var songFolderDetailsSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_FOLDER_DETAILS_SORT_ORDER,
            SortOrder.SongFolderDetailsSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit{
            putString(SONG_FOLDER_DETAILS_SORT_ORDER, value)
        }

    var videoFolderDetailsSortOrder
        get() = sharedPreferences.getStringOrDefault(
            VIDEO_FOLDER_DETAILS_SORT_ORDER,
            SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(VIDEO_FOLDER_DETAILS_SORT_ORDER, value)
        }



    var videoFolderGridSize
        get() = sharedPreferences.getInt(
            VIDEO_FOLDER_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(VIDEO_FOLDER_GRID_SIZE, value)
        }

    var videoGridSize
        get() = sharedPreferences.getInt(
            VIDEO_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(VIDEO_GRID_SIZE, value)
        }

    var songGridSize
        get() = sharedPreferences.getInt(
            SONG_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_SIZE, value)
        }

    var songFolderGridSize
        get() = sharedPreferences.getInt(
            SONG_FOLDER_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_FOLDER_GRID_SIZE, value)
        }

    var albumGridSize: Int
        get() = sharedPreferences.getInt(
            ALBUM_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE, value)
        }

    var artistGridSize
        get() = sharedPreferences.getInt(
            ARTIST_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(ARTIST_GRID_SIZE, value)
        }

    var favoriteGridSize
        get() = sharedPreferences.getInt(
            FAVORITE_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(FAVORITE_GRID_SIZE, value)
        }

    var playlistGridSize
        get() = sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE,
            App.getContext().getIntRes(
                R.integer.default_list_columns
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(PLAYLIST_GRID_SIZE, value)
        }



    var viewPagerPosition
        get() = sharedPreferences.getInt(
            VIEWPAGER2_TAB_POSITION,
            App.getContext().getIntRes(
                R.integer.default_tab_position
            )
        )
        set(value) = sharedPreferences.edit{
            putInt(VIEWPAGER2_TAB_POSITION, value)
        }

    var appbarMode: Int
        get() = sharedPreferences.getInt(
            APPBAR_MODE,
            App.getContext().getIntRes(
                R.integer.default_tab_position
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(APPBAR_MODE, value)
        }

    var brightNess: Int
        get() = sharedPreferences.getInt(
            BRIGHTNESS,
            App.getContext().getIntRes(
                R.integer.default_brightness
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(BRIGHTNESS, value)
        }

    var mediaLoudNess: Int
        get() = sharedPreferences.getInt(
            MEDIA_LOUDNESS,
            App.getContext().getIntRes(
                R.integer.default_loudness
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(MEDIA_LOUDNESS, value)
        }

    var exoLoudNess: Int
        get() = sharedPreferences.getInt(
            EXO_LOUDNESS,
            App.getContext().getIntRes(
                R.integer.default_loudness
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(EXO_LOUDNESS, value)
        }


    var isSongOrVideo: Int
        get() = sharedPreferences.getInt(
            IS_SONG_OR_VIDEO,
            App.getContext().getIntRes(
                R.integer.default_int
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(IS_SONG_OR_VIDEO, value)
        }

    var videoProgressMillis: Long
        get() = sharedPreferences.getLong(
            VIDEO_PROGRESS_MILLIS,
            0L
        )
        set(value) = sharedPreferences.edit {
            putLong(VIDEO_PROGRESS_MILLIS, value)
        }


    var nowPlayingFragment: Int
        get() = sharedPreferences.getInt(
            NOW_PLAYING_FRAGMENT,
            App.getContext().getIntRes(
                R.integer.default_int
            )
        )
        set(value) = sharedPreferences.edit {
            putInt(NOW_PLAYING_FRAGMENT, value)
        }
}