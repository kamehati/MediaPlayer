package com.example.elect.mediaplayer.fragments.music.artist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.model.Artist
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArtistDetailsViewModel(
    private val repository: RealRepository,
    private val artistId: Long
) : ViewModel() {
    private val artistDetails = MutableLiveData<Artist>()

    init {
        fetchArtist()
    }

    fun forceReload() = fetchArtist()

    private fun fetchArtist() {
        viewModelScope.launch(Dispatchers.IO) {
            artistDetails.postValue(
                repository.artistByIdAsync(artistId)
            )
        }
    }

    fun getArtist(): LiveData<Artist> = artistDetails
}