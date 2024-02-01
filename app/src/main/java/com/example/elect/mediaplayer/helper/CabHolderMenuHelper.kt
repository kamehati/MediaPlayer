package com.example.elect.mediaplayer.helper

import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object CabHolderMenuHelper: KoinComponent {


    fun handleMenuClick(
        activity: FragmentActivity,
        medias: List<Media>,
        menuItemId: Int
    ): Boolean {
        when (menuItemId) {

            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog
                            .create(playlists, medias, activity as MainActivity)
                            .show(
                                activity.supportFragmentManager,

                                "ADD_PLAYLIST"
                            )
                    }
                }
                return true
            }
        }
        return false
    }
}