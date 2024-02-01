package com.example.elect.mediaplayer.fragments.music.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumDetailsViewModel(
    private val repository: RealRepository,
    private val albumId: Long
) : ViewModel() {

    private val albumDetails = MutableLiveData<Album>()

    init {
        fetchAlbum()
    }

    fun forceReload() = fetchAlbum()

    private fun fetchAlbum() {
        viewModelScope.launch(Dispatchers.IO) {
            albumDetails.postValue(
                repository.albumByIdAsync(albumId)
            )
        }
    }

    fun getAlbum(): LiveData<Album> = albumDetails
}