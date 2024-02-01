package com.example.elect.mediaplayer.fragments.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.db.*
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistDetailsViewModel(
    private val realRepository: RealRepository,
    private var playlist: PlaylistWithMedias
) : ViewModel(){
    private val playListMedias = MutableLiveData<List<MediaEntity>>()

    init {
        loadContent()
    }

    private fun loadContent() =
        viewModelScope.launch(Dispatchers.IO) {
            fetchPlaylistMediaEntity()
        }

    fun getSongEntity(): LiveData<List<MediaEntity>> {
        return playListMedias
    }

    fun getSongs() : LiveData<List<SongEntity>> =
        realRepository.playlistSongs(
            playlist.playlistEntity.playlistId
        )

    fun getMedias() : LiveData<List<MediaEntity>> =
        realRepository.playlistMedias(
            playlist.playlistEntity.playlistId
        )

    fun playlistExists():LiveData<Boolean> =
        realRepository.checkPlaylistExists(playlist.playlistEntity.playlistId)

    fun forceReload() =
        viewModelScope.launch(Dispatchers.IO) {
            fetchPlaylistMediaEntity()
        }

    private suspend fun fetchPlaylistMediaEntity(){
        playListMedias.postValue(
            realRepository.fetchPlaylistMediaEntity(
                playlist.playlistEntity.playlistId
            )
        )
    }
}