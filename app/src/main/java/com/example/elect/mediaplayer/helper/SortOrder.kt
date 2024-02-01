package com.example.elect.mediaplayer.helper

import android.provider.MediaStore

class SortOrder {

    interface VideoFolderSortOrder {

        companion object {


            const val VIDEO_FOLDER_A_Z = MediaStore.Video.Media.DEFAULT_SORT_ORDER


            const val VIDEO_FOLDER_Z_A = "$VIDEO_FOLDER_A_Z DESC"
        }
    }

    interface VideoSortOrder {

        companion object {


            const val VIDEO_A_Z = MediaStore.Video.Media.DEFAULT_SORT_ORDER


            const val VIDEO_Z_A = "$VIDEO_A_Z DESC"
        }
    }

    interface SongSortOrder {

        companion object {


            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val SONG_Z_A = "$SONG_A_Z DESC"


            const val SONG_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER


            const val SONG_ALBUM = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER


            const val SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC"




            const val SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC"


            const val SONG_DATE_MODIFIED = MediaStore.Audio.Media.DATE_MODIFIED + " DESC"
        }
    }

    interface SongFolderSortOrder {

        companion object {


            const val SONG_FOLDER_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val SONG_FOLDER_Z_A = "$SONG_FOLDER_A_Z DESC"
        }
    }

    interface AlbumSortOrder {

        companion object {


            const val ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER


            const val ALBUM_Z_A = "$ALBUM_A_Z DESC"


            const val ALBUM_NUMBER_OF_SONGS =
                MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS + " DESC"
        }
    }

    interface ArtistSortOrder {

        companion object {


            const val ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER


            const val ARTIST_Z_A = "$ARTIST_A_Z DESC"
        }
    }

    interface FavoriteSortOrder {

        companion object {


            const val FAVORITE_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val FAVORITE_Z_A = "$FAVORITE_A_Z DESC"
        }
    }

    interface PlaylistSortOrder {

        companion object {


            const val PLAYLIST_A_Z = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER


            const val PLAYLIST_Z_A = "$PLAYLIST_A_Z DESC"


            const val PLAYLIST_SONG_COUNT_ASC = "playlist_song_count"


            const val PLAYLIST_SONG_COUNT_DESC = "$PLAYLIST_SONG_COUNT_ASC DESC"
        }
    }

    interface AlbumDetailsSortOrder {
        companion object {


            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val SONG_Z_A = "$SONG_A_Z DESC"
        }
    }

    interface ArtistDetailsSortOrder {
        companion object {


            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val SONG_Z_A = "$SONG_A_Z DESC"
        }
    }

    interface SongFolderDetailsSortOrder {
        companion object {


            const val SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER


            const val SONG_Z_A = "$SONG_A_Z DESC"
        }
    }

    interface VideoFolderDetailsSortOrder {
        companion object {


            const val VIDEO_A_Z = MediaStore.Video.Media.DEFAULT_SORT_ORDER


            const val VIDEO_Z_A = "$VIDEO_A_Z DESC"
        }
    }
}