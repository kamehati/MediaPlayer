package com.example.elect.mediaplayer.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.elect.mediaplayer.FAVORITE_SORT_ORDER
import com.example.elect.mediaplayer.db.*
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.helper.SortOrder.FavoriteSortOrder.Companion.FAVORITE_A_Z
import com.example.elect.mediaplayer.helper.SortOrder.FavoriteSortOrder.Companion.FAVORITE_Z_A
import com.example.elect.mediaplayer.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_A_Z
import com.example.elect.mediaplayer.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_SONG_COUNT_ASC
import com.example.elect.mediaplayer.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_SONG_COUNT_DESC
import com.example.elect.mediaplayer.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_Z_A
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.PreferenceUtil

interface RoomRepository {
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity>
    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun insertMedias(medias: List<MediaEntity>)
    suspend fun playlistWithSongs():List<PlaylistWithSongs>
    suspend fun playlistWithMedias():List<PlaylistWithMedias>
    suspend fun playlists(): List<PlaylistEntity>
    suspend fun deletePlaylistEntities(playlistEntities: List<PlaylistEntity>)
    suspend fun renamePlaylistEntity(playlistId: Long, name: String)
    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>)
    suspend fun deletePlaylistMedias(playlists: List<PlaylistEntity>)

    suspend fun favorites(): List<FavoriteEntity>
    suspend fun mediaFavorites(): List<MediaFavoriteEntity>
    suspend fun insertOrUpdateFavoriteEntity(songId: Long, isFavorite: Boolean, favoriteEntity: FavoriteEntity)
    suspend fun insertOrUpdateMediaFavoriteEntity(mediaId: Long, isFavorite: Boolean, mediaFavoriteEntity: MediaFavoriteEntity)
    suspend fun isMediaFavoriteEntity(mediaId: Long): Boolean
    fun isMediaFavorite(mediaId: Long): Boolean

    suspend fun playlistSongs(playlistId: Long): PlaylistWithSongs

    fun getSongs(playListId: Long): LiveData<List<SongEntity>>
    fun getMedias(playListId: Long): LiveData<List<MediaEntity>>

    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>)
    suspend fun deleteMediasInPlaylist(medias: List<MediaEntity>)

    fun checkPlaylistExists(playlistId: Long): LiveData<Boolean>

    suspend fun playlistSongEntity(playlistId: Long):List<SongEntity>
    suspend fun playlistMediaEntity(playlistId: Long):List<MediaEntity>
}

class RealRoomRepository(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao
) : RoomRepository {

    @WorkerThread
    override suspend fun createPlaylist(
        playlistEntity: PlaylistEntity
    ): Long = playlistDao.createPlaylist(playlistEntity)


    @WorkerThread
    override suspend fun checkPlaylistExists(
        playlistName: String
    ): List<PlaylistEntity> =
        playlistDao.playlist(playlistName)

    override fun checkPlaylistExists(
        playlistId: Long
    ): LiveData<Boolean> =
        playlistDao.checkPlaylistExists(playlistId)

    @WorkerThread
    override suspend fun insertSongs(
        songs: List<SongEntity>
    ) {
        playlistDao.insertSongsToPlaylist(songs)
    }

    @WorkerThread
    override suspend fun insertMedias(
        medias: List<MediaEntity>
    ) {
        playlistDao.insertMediasToPlaylist(medias)
    }

    @WorkerThread
    override suspend fun playlistWithSongs()
    : List<PlaylistWithSongs> =
        when(PreferenceUtil.playlistSortOrder){
            PLAYLIST_A_Z -> {
                playlistDao.playlistsWithSongs().sortedBy {
                    it.playlistEntity.playlistName
                }
            }
            PLAYLIST_Z_A -> {
                playlistDao.playlistsWithSongs().sortedByDescending {
                    it.playlistEntity.playlistName
                }
            }
            PLAYLIST_SONG_COUNT_ASC -> {
                playlistDao.playlistsWithSongs().sortedBy {
                    it.songs.size
                }
            }
            PLAYLIST_SONG_COUNT_DESC -> {
                playlistDao.playlistsWithSongs().sortedByDescending {
                    it.songs.size
                }
            }
            else -> {
                playlistDao.playlistsWithSongs().sortedBy {
                    it.playlistEntity.playlistName
                }
            }
        }

    override suspend fun playlistWithMedias()
    : List<PlaylistWithMedias> =
        when(PreferenceUtil.playlistSortOrder){
            PLAYLIST_A_Z -> {
                playlistDao.playlistsWithMedias().sortedBy {
                    it.playlistEntity.playlistName
                }
            }
            PLAYLIST_Z_A -> {
                playlistDao.playlistsWithMedias().sortedByDescending {
                    it.playlistEntity.playlistName
                }
            }
            PLAYLIST_SONG_COUNT_ASC -> {
                playlistDao.playlistsWithMedias().sortedBy {
                    it.medias.size
                }
            }
            PLAYLIST_SONG_COUNT_DESC -> {
                playlistDao.playlistsWithMedias().sortedByDescending {
                    it.medias.size
                }
            }
            else -> {
                playlistDao.playlistsWithMedias().sortedBy {
                    it.playlistEntity.playlistName
                }
            }
        }

    @WorkerThread
    override suspend fun playlists()
    : List<PlaylistEntity> = playlistDao.playlists()

    override suspend fun deletePlaylistEntities(
        playlistEntities: List<PlaylistEntity>
    ) = playlistDao.deletePlaylists(playlistEntities)

    override suspend fun renamePlaylistEntity(
        playlistId: Long, name: String
    ) = playlistDao.renamePlaylist(playlistId, name)

    override suspend fun deletePlaylistSongs(
        playlists: List<PlaylistEntity>
    ) = playlists.forEach {
        playlistDao.deletePlaylistSongs(it.playlistId)
    }

    override suspend fun deletePlaylistMedias(
        playlists: List<PlaylistEntity>
    ) = playlists.forEach {
        playlistDao.deletePlaylistMedias(it.playlistId)
    }


    @WorkerThread
    override suspend fun favorites()
    : List<FavoriteEntity> =
        when(PreferenceUtil.favoriteSortOrder){
            FAVORITE_A_Z -> {
                favoriteDao.favoriteEntities().sortedBy {
                    it.title
                }
            }
            FAVORITE_Z_A -> {
                favoriteDao.favoriteEntities().sortedByDescending {
                    it.title
                }
            }
            else -> {
                favoriteDao.favoriteEntities().sortedBy {
                    it.title
                }
            }
        }


    @WorkerThread
    override suspend fun mediaFavorites()
    : List<MediaFavoriteEntity> =
        when(PreferenceUtil.favoriteSortOrder){
            FAVORITE_A_Z -> {
                favoriteDao.mediaFavoriteEntities().sortedBy {
                    it.title
                }
            }
            FAVORITE_Z_A -> {
                favoriteDao.mediaFavoriteEntities().sortedByDescending {
                    it.title
                }
            }
            else -> {
                favoriteDao.mediaFavoriteEntities().sortedBy {
                    it.title
                }
            }
        }



    @WorkerThread
    override suspend fun insertOrUpdateFavoriteEntity(
        songId: Long,
        isFavorite: Boolean,
        favoriteEntity: FavoriteEntity
    ) {
        favoriteDao.insertOrUpdateFavoriteEntity(
            songId,
            isFavorite,
            favoriteEntity
        )
    }

    @WorkerThread
    override suspend fun insertOrUpdateMediaFavoriteEntity(
        mediaId: Long,
        isFavorite: Boolean,
        mediaFavoriteEntity: MediaFavoriteEntity
    ) {
        favoriteDao.insertOrUpdateMediaFavoriteEntity(
            mediaId,
            isFavorite,
            mediaFavoriteEntity
        )
    }

    @WorkerThread
    override suspend fun isMediaFavoriteEntity(
        mediaId: Long
    ): Boolean {
        return favoriteDao.isMediaFavoriteEntity(mediaId)
    }

    override fun isMediaFavorite(
        mediaId: Long
    ): Boolean {
        return favoriteDao.isMediaFavorite(mediaId)
    }

    @WorkerThread
    override suspend fun playlistSongs(
        playlistId: Long
    ): PlaylistWithSongs =
        playlistDao.playlistsWithSongs(playlistId)


    override fun getSongs(
        playListId: Long
    ): LiveData<List<SongEntity>> =
        playlistDao.songsFromPlaylist(playListId)

    override fun getMedias(
        playListId: Long
    ): LiveData<List<MediaEntity>> =
        playlistDao.mediasFromPlaylist(playListId)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) {
        songs.forEach {
            playlistDao.deleteSongFromPlaylist(it.playlistCreatorId, it.id)
        }
    }

    override suspend fun deleteMediasInPlaylist(medias: List<MediaEntity>) {
        medias.forEach {
            playlistDao.deleteMediaFromPlaylist(it.playlistCreatorId, it.id)
        }
    }

    @WorkerThread
    override suspend fun playlistSongEntity(
        playlistId: Long
    ): List<SongEntity> =
        playlistDao.songsFromPlaylistId(playlistId)

    @WorkerThread
    override suspend fun playlistMediaEntity(
        playlistId: Long
    ): List<MediaEntity> =
        playlistDao.mediasFromPlaylistId(playlistId)
}