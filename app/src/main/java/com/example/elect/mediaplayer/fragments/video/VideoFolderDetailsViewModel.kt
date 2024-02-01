package com.example.elect.mediaplayer.fragments.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elect.mediaplayer.model.VideoFolder
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoFolderDetailsViewModel(
    private val repository: RealRepository,
    private val folderId: Long
) : ViewModel() {
    private val videoFolder = MutableLiveData<VideoFolder>()

    init {
        fetchVideoFolder()
    }

    fun forceReload() = fetchVideoFolder()

    private fun fetchVideoFolder() {
        viewModelScope.launch(Dispatchers.IO) {
            videoFolder.postValue(
                repository.mediaVideoFolderByIdAsync(folderId)
            )
        }
    }

    fun getVideoFolder(): LiveData<VideoFolder> = videoFolder
}