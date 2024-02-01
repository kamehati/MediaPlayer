package com.example.elect.mediaplayer.helper

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object AlbumMenuHelper : KoinComponent{
    const val MENU_RES = R.menu.menu_item_folder

    fun handleMenuClick(
        activity: FragmentActivity,
        album: Album,
        menuItemId: Int
    ): Boolean{
        when(menuItemId){

            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO)
                    .launch {
                        val playlists = get<RealRepository>()
                            .fetchPlaylists()
                        withContext(Dispatchers.Main){
                            AddToPlaylistDialog
                                .create(playlists, album.medias, activity as MainActivity)
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

    abstract class OnClickAlbumMenu(
        private val activity: FragmentActivity
    ) : View.OnClickListener,
        PopupMenu.OnMenuItemClickListener
    {

        open val menuRes: Int
            get() = MENU_RES
        abstract val album: Album


        override fun onClick(v: View) {

            val wrapper = ContextThemeWrapper(
                activity,
                R.style.MaterialPopupMenuStyle
            )
            val popupMenu = PopupMenu(wrapper, v)

            popupMenu.inflate(menuRes)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        override fun onMenuItemClick(
            item: MenuItem
        ): Boolean {
            return handleMenuClick(
                activity,
                album,
                item.itemId
            )
        }
    }
}