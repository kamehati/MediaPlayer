package com.example.elect.mediaplayer.helper

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.db.PlaylistWithMedias
import com.example.elect.mediaplayer.db.PlaylistWithSongs
import com.example.elect.mediaplayer.db.toMedias
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.dialogs.DeletePlaylistDialog
import com.example.elect.mediaplayer.dialogs.RenamePlaylistDialog
import com.example.elect.mediaplayer.model.SongFolder
import com.example.elect.mediaplayer.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object PlaylistMenuHelper : KoinComponent {
    const val MENU_RES = R.menu.menu_item_playlist

    fun handleMenuClick(
        activity: FragmentActivity,
        playlistWithMedias: PlaylistWithMedias,
        menuItemId: Int
    ): Boolean {
        when (menuItemId) {

            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO)
                    .launch {
                        val playlists = get<RealRepository>()
                            .fetchPlaylists()
                        withContext(Dispatchers.Main){
                            AddToPlaylistDialog
                                .create(playlists, playlistWithMedias.medias.toMedias(), activity as MainActivity)
                                .show(
                                    activity.supportFragmentManager,
                                    "ADD_PLAYLIST"
                                )
                        }
                    }

                return true
            }


            R.id.action_rename_playlist -> {
                RenamePlaylistDialog
                    .create(playlistWithMedias.playlistEntity)
                    .show(
                        activity.supportFragmentManager,
                        "RENAME_PLAYLIST"
                    )
                return true
            }


            R.id.action_delete_playlist -> {
                DeletePlaylistDialog
                    .create(playlistWithMedias.playlistEntity)
                    .show(
                        activity.supportFragmentManager,
                        "DELETE_PLAYLIST"
                    )
                return true
            }
        }
        return false
    }

    abstract class OnClickPlaylistMenu(
        private val activity: FragmentActivity
    ) : View.OnClickListener,
        PopupMenu.OnMenuItemClickListener
    {

        open val menuRes: Int
            get() = MENU_RES
        abstract val playlist: PlaylistWithMedias


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
                playlist,
                item.itemId
            )
        }
    }
}