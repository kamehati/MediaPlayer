package com.example.elect.mediaplayer.fragments

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.App
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.db.*
import com.example.elect.mediaplayer.model.*
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val repository: RealRepository
) : ViewModel() {

    private val songs = MutableLiveData<List<Song>>()
    private val mediaSongs = MutableLiveData<List<Media>>()
    private val songFolders = MutableLiveData<List<SongFolder>>()
    private val videoFolders = MutableLiveData<List<VideoFolder>>()
    private val albums = MutableLiveData<List<Album>>()
    private val artists = MutableLiveData<List<Artist>>()
    private val playlists = MutableLiveData<List<PlaylistWithSongs>>()
    private val mediaPlaylists = MutableLiveData<List<PlaylistWithMedias>>()
    private val favoriteEntity = MutableLiveData<List<FavoriteEntity>>()
    private val mediaFavoriteEntity = MutableLiveData<List<MediaFavoriteEntity>>()
    private val favoriteSongs = MutableLiveData<List<Song>>()
    private val favoriteMedias = MutableLiveData<List<Media>>()


    init {
        loadLibraryContent()
    }

    private fun loadLibraryContent() =
        viewModelScope.launch(IO) {
            fetchSongs()
            fetchMediaSongs()
            fetchVideoFolders()
            fetchSongFolders()
            fetchAlbums()
            fetchArtists()
            fetchPlaylists()
            fetchMediaPlaylists()
            fetchFavoriteEntities()
            fetchFavoriteSongs()
            fetchMediaFavoriteEntities()
            fetchFavoriteMedias()
        }

    fun getSongs(): LiveData<List<Song>> {
        return songs
    }

    fun getMediaSongs(): LiveData<List<Media>> {
        return mediaSongs
    }

    private suspend fun fetchSongs() {

        songs.postValue(repository.allSongs())
    }

    private suspend fun fetchMediaSongs() {

        mediaSongs.postValue(repository.allMediaSongs())
    }

    fun getSongFolders(): LiveData<List<SongFolder>> {
        return songFolders
    }

    private suspend fun fetchSongFolders() {
        songFolders.postValue(repository.allSongFolders())
    }

    fun forceReload(reloadType: ReloadType) =
        viewModelScope.launch(IO) {
            when(reloadType){
                ReloadType.VideoFolders -> fetchVideoFolders()
                ReloadType.Songs -> {
                    fetchSongs()
                    fetchMediaSongs()
                }
                ReloadType.SongFolders -> fetchSongFolders()
                ReloadType.Albums -> fetchAlbums()
                ReloadType.Artists -> fetchArtists()
                ReloadType.Playlists -> {
                    fetchPlaylists()
                    fetchMediaPlaylists()
                }
                ReloadType.Favorites -> {
                    fetchFavoriteEntities()
                    fetchFavoriteSongs()
                }
                ReloadType.FavoriteMedias -> {
                    fetchMediaFavoriteEntities()
                    fetchFavoriteMedias()
                }
            }
        }

    fun getVideoFolders(): LiveData<List<VideoFolder>> {
        return videoFolders
    }

    private suspend fun fetchVideoFolders() {
        videoFolders.postValue(
            repository.allVideoFolders()
        )
    }

    fun getAlbums(): LiveData<List<Album>> {
        return albums
    }

    private suspend fun fetchAlbums() {
        albums.postValue(repository.allAlbums())
    }

    fun getArtists(): LiveData<List<Artist>> {
        return artists
    }

    private suspend fun fetchArtists() {
        artists.postValue(repository.allArtists())
    }

    private suspend fun fetchPlaylists() {
        playlists.postValue(
            repository.fetchPlaylistWithSongs()
        )
    }

    private suspend fun fetchMediaPlaylists() {
        mediaPlaylists.postValue(
            repository.fetchPlaylistWithMedias()
        )
    }

    fun getPlaylists(): LiveData<List<PlaylistWithSongs>> {
        return playlists
    }

    fun getMediaPlaylists(): LiveData<List<PlaylistWithMedias>> {
        return mediaPlaylists
    }

    suspend fun insertSongs(songs: List<SongEntity>) =
        repository.insertSongs(songs)

    suspend fun insertMedias(medias: List<MediaEntity>) =
        repository.insertMedias(medias)

    private suspend fun checkPlaylistsExists(
        playlistsName: String
    ): List<PlaylistEntity> =
        repository.checkPlaylistExists(playlistsName)

    private suspend fun createPlaylist(
        playlistEntity: PlaylistEntity
    ): Long = repository.createPlaylist(playlistEntity)

    fun addToPlaylist(
        playlistName: String,
        songs: List<Song>
    ){
        viewModelScope.launch(IO) {
            val playlists = checkPlaylistsExists(playlistName)

            if(playlists.isEmpty()){
                val playlistId: Long = createPlaylist(
                    PlaylistEntity(
                        playlistName = playlistName
                    )
                )

                insertSongs(
                    songs.map {
                        it.toSongEntity(
                            playlistId
                        )
                    }
                )

                forceReload(ReloadType.Playlists)


            }
            else {
                val playlist = playlists.firstOrNull()

                if(playlist != null){
                    insertSongs(
                        songs.map {
                            it.toSongEntity(
                                playlistId = playlist.playlistId,
                            )
                        }
                    )
                }


            }
        }
    }



    fun addMediaToPlaylist(
        playlistName: String,
        medias: List<Media>
    ){
        viewModelScope.launch(IO) {
            val playlists = checkPlaylistsExists(playlistName)

            if(playlists.isEmpty()){
                val playlistId: Long = createPlaylist(
                    PlaylistEntity(
                        playlistName = playlistName
                    )
                )

                insertMedias(
                    medias.map {
                        it.toMediaEntity(
                            playlistId
                        )
                    }
                )

                forceReload(ReloadType.Playlists)
            }
            else {
                val playlist = playlists.firstOrNull()

                if(playlist != null){
                    insertMedias(
                        medias.map {
                            it.toMediaEntity(
                                playlistId = playlist.playlistId,
                            )
                        }
                    )
                }
            }
        }
    }

    fun renameRoomPlaylist(playListId: Long, name: String) =
        viewModelScope.launch(IO) {
            repository.renameRoomPlaylist(playListId, name)
        }

    fun deleteSongsFromPlaylist(playlists: List<PlaylistEntity>) =
        viewModelScope.launch(IO) {
            repository.deletePlaylistSongs(playlists)
        }

    fun deleteMediasFromPlaylist(playlists: List<PlaylistEntity>) =
        viewModelScope.launch(IO) {
            repository.deletePlaylistMedias(playlists)
        }

    fun deleteSongsInPlaylist(songs: List<SongEntity>) {
        viewModelScope.launch(IO) {
            repository.deleteSongsInPlaylist(songs)
            forceReload(ReloadType.Playlists)
        }
    }

    fun deleteMediasInPlaylist(medias: List<MediaEntity>) {
        viewModelScope.launch(IO) {
            repository.deleteMediasInPlaylist(medias)
            forceReload(ReloadType.Playlists)
        }
    }

    fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) =
        viewModelScope.launch(IO) {
            repository.deleteRoomPlaylist(playlists)
        }


    private suspend fun fetchFavoriteSongs() {
        favoriteSongs.postValue(
            repository.favorites().distinct().toSongs()
        )
    }

    fun getFavoriteSongs() : LiveData<List<Song>> {
        return favoriteSongs
    }

    private suspend fun fetchFavoriteMedias() {
        favoriteMedias.postValue(
            repository.mediaFavorites().distinct().toMedias()
        )
    }

    fun getFavoriteMedias() : LiveData<List<Media>> {
        return favoriteMedias
    }

    private suspend fun fetchFavoriteEntities() {
        favoriteEntity.postValue(
            repository.favorites().distinct()
        )
    }

    fun getFavoriteEntities() : LiveData<List<FavoriteEntity>> {
        return favoriteEntity
    }

    private suspend fun fetchMediaFavoriteEntities() {
        mediaFavoriteEntity.postValue(
            repository.mediaFavorites().distinct()
        )
    }

    fun getMediaFavoriteEntities() : LiveData<List<MediaFavoriteEntity>> {
        return mediaFavoriteEntity
    }


    suspend fun insertOrUpdateFavoriteEntity(
        song: Song,
        isFavorite: Boolean
    ) {
        val favoriteEntity =
            song.toFavoriteEntity(isFavorite)

        repository.insertOrUpdateFavoriteEntity(song.id, isFavorite, favoriteEntity)
    }


    suspend fun insertOrUpdateMediaFavoriteEntity(
        media: Media,
        isFavorite: Boolean
    ) {
        val mediaFavoriteEntity =
            media.toMediaFavoriteEntity(isFavorite)

        repository.insertOrUpdateMediaFavoriteEntity(media.id, isFavorite, mediaFavoriteEntity)
    }

    suspend fun isMediaFavoriteEntity(
        mediaId: Long
    ): Boolean {
        return repository.isMediaFavoriteEntity(mediaId)
    }

    fun isMediaFavorite(
        mediaId: Long
    ): Boolean {
        return repository.isMediaFavorite(mediaId)
    }
}

enum class ReloadType {
    VideoFolders,
    Songs,
    SongFolders,
    Albums,
    Artists,
    Favorites,
    Playlists,
    FavoriteMedias
}