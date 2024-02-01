package com.example.elect.mediaplayer.fragments.music.folder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.model.SongFolder
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongFolderDetailsViewModel(
    private val repository: RealRepository,
    private val folderId: Long
) : ViewModel() {
    private val songFolder = MutableLiveData<SongFolder>()

    init {
        fetchSongFolder()
    }

    fun forceReload() = fetchSongFolder()

    private fun fetchSongFolder() {
        viewModelScope.launch(Dispatchers.IO) {
            songFolder.postValue(
                repository.songFolderByIdAsync(folderId)
            )
        }
    }

    fun getSongFolder(): LiveData<SongFolder> = songFolder
}