package com.example.elect.mediaplayer.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.elect.mediaplayer.db.*
import com.example.elect.mediaplayer.model.*

interface Repository{
    suspend fun allSongs(): List<Song>
    suspend fun allMediaSongs(): List<Media>
    suspend fun allSongFolders(): List<SongFolder>
    suspend fun allVideoFolders(): List<VideoFolder>
    suspend fun allAlbums(): List<Album>
    suspend fun allArtists(): List<Artist>
    suspend fun favoriteSong(songIds: List<Long>): List<Song>

    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun insertMedias(medias: List<MediaEntity>)
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity>
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
    suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs>
    suspend fun fetchPlaylistWithMedias(): List<PlaylistWithMedias>
    suspend fun fetchPlaylists(): List<PlaylistEntity>
    suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>)
    suspend fun renameRoomPlaylist(playlistId: Long, name: String)
    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>)
    suspend fun deletePlaylistMedias(playlists: List<PlaylistEntity>)

    suspend fun favorites(): List<FavoriteEntity>
    suspend fun mediaFavorites(): List<MediaFavoriteEntity>
    suspend fun insertOrUpdateFavoriteEntity(songId: Long, isFavorite: Boolean, favoriteEntity: FavoriteEntity)
    suspend fun insertOrUpdateMediaFavoriteEntity(mediaId: Long, isFavorite: Boolean, mediaFavoriteEntity: MediaFavoriteEntity)
    suspend fun isMediaFavoriteEntity(mediaId: Long): Boolean
    fun isMediaFavorite(mediaId: Long): Boolean

    suspend fun fetchPlaylistSongs(playlistId: Long): PlaylistWithSongs

    suspend fun albumByIdAsync(albumId: Long): Album
    suspend fun artistByIdAsync(artistId: Long): Artist
    suspend fun songFolderByIdAsync(folderId: Long): SongFolder
    suspend fun videoFolderByIdAsync(folderId: Long): VideoFolder
    suspend fun mediaVideoFolderByIdAsync(folderId: Long): VideoFolder

    fun playlistSongs(playListId: Long): LiveData<List<SongEntity>>
    fun playlistMedias(playListId: Long): LiveData<List<MediaEntity>>

    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>)
    suspend fun deleteMediasInPlaylist(medias: List<MediaEntity>)

    fun checkPlaylistExists(playlistId: Long): LiveData<Boolean>

    suspend fun fetchPlaylistSongEntity(playlistId: Long): List<SongEntity>
    suspend fun fetchPlaylistMediaEntity(playlistId: Long): List<MediaEntity>
}

class RealRepository(
    private val context: Context,
    private val songRepository: SongRepository,
    private val videoRepository: VideoRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val roomRepository: RoomRepository,
    private val mediaRepository: MediaRepository
): Repository {

    override suspend fun allSongs(): List<Song> =
        songRepository.songs()

    override suspend fun allMediaSongs(): List<Media> =
        songRepository.mediaSongs()

    override suspend fun allSongFolders(): List<SongFolder> =
        songRepository.songFolders()

    override suspend fun allVideoFolders(): List<VideoFolder> =
        videoRepository.videoFolders()

    override suspend fun allAlbums(): List<Album> =
        albumRepository.albumBuckets()

    override suspend fun allArtists(): List<Artist> =
        artistRepository.artistBuckets()

    override suspend fun favoriteSong(songIds: List<Long>): List<Song> =
        songRepository.songs(songIds)



    override suspend fun insertSongs(
        songs: List<SongEntity>
    ) = roomRepository.insertSongs(songs)

    override suspend fun insertMedias(
        medias: List<MediaEntity>
    ) = roomRepository.insertMedias(medias)

    override suspend fun checkPlaylistExists(
        playlistName: String
    ): List<PlaylistEntity> =
        roomRepository.checkPlaylistExists(playlistName)

    override fun checkPlaylistExists(
        playlistId: Long
    ): LiveData<Boolean> =
        roomRepository.checkPlaylistExists(playlistId)

    override suspend fun createPlaylist(
        playlistEntity: PlaylistEntity
    ): Long = roomRepository.createPlaylist(playlistEntity)

    override suspend fun fetchPlaylistWithSongs()
    : List<PlaylistWithSongs> = roomRepository.playlistWithSongs()

    override suspend fun fetchPlaylistWithMedias()
    : List<PlaylistWithMedias> = roomRepository.playlistWithMedias()

    override suspend fun fetchPlaylists(): List<PlaylistEntity> =
        roomRepository.playlists()

    override suspend fun deleteRoomPlaylist(
        playlists: List<PlaylistEntity>
    ) = roomRepository.deletePlaylistEntities(playlists)

    override suspend fun renameRoomPlaylist(
        playlistId: Long, name: String
    ) = roomRepository.renamePlaylistEntity(playlistId, name)

    override suspend fun deletePlaylistSongs(
        playlists: List<PlaylistEntity>
    ) = roomRepository.deletePlaylistSongs(playlists)

    override suspend fun deletePlaylistMedias(
        playlists: List<PlaylistEntity>
    ) = roomRepository.deletePlaylistMedias(playlists)


    override suspend fun favorites()
    : List<FavoriteEntity> = roomRepository.favorites()

    override suspend fun mediaFavorites()
    : List<MediaFavoriteEntity> = roomRepository.mediaFavorites()

    override suspend fun insertOrUpdateFavoriteEntity(
        songId: Long,
        isFavorite: Boolean,
        favoriteEntity: FavoriteEntity
    ) = roomRepository.insertOrUpdateFavoriteEntity(
        songId,
        isFavorite,
        favoriteEntity
    )

    override suspend fun insertOrUpdateMediaFavoriteEntity(
        mediaId: Long,
        isFavorite: Boolean,
        mediaFavoriteEntity: MediaFavoriteEntity
    ) = roomRepository.insertOrUpdateMediaFavoriteEntity(
        mediaId,
        isFavorite,
        mediaFavoriteEntity
    )

    override suspend fun isMediaFavoriteEntity(
        mediaId: Long
    ): Boolean = roomRepository.isMediaFavoriteEntity(mediaId)

    override fun isMediaFavorite(
        mediaId: Long
    ): Boolean = roomRepository.isMediaFavorite(mediaId)

    override suspend fun fetchPlaylistSongs(
        playlistId: Long
    ): PlaylistWithSongs = roomRepository.playlistSongs(playlistId)

    override suspend fun albumByIdAsync(albumId: Long): Album =
        albumRepository.album(albumId)

    override suspend fun artistByIdAsync(artistId: Long): Artist =
        artistRepository.artist(artistId)

    override suspend fun songFolderByIdAsync(folderId: Long): SongFolder =
        songRepository.songFolder(folderId)

    override suspend fun videoFolderByIdAsync(folderId: Long): VideoFolder =
        videoRepository.videoFolder(folderId)

    override suspend fun mediaVideoFolderByIdAsync(folderId: Long): VideoFolder =
        mediaRepository.videoFolder(folderId)


    override fun playlistSongs(
        playListId: Long
    ): LiveData<List<SongEntity>> =
        roomRepository.getSongs(playListId)

    override fun playlistMedias(
        playListId: Long
    ): LiveData<List<MediaEntity>> =
        roomRepository.getMedias(playListId)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) =
        roomRepository.deleteSongsInPlaylist(songs)

    override suspend fun deleteMediasInPlaylist(medias: List<MediaEntity>) =
        roomRepository.deleteMediasInPlaylist(medias)

    override suspend fun fetchPlaylistSongEntity(
        playlistId: Long
    ): List<SongEntity> =
        roomRepository.playlistSongEntity(playlistId)

    override suspend fun fetchPlaylistMediaEntity(
        playlistId: Long
    ): List<MediaEntity> =
        roomRepository.playlistMediaEntity(playlistId)
}