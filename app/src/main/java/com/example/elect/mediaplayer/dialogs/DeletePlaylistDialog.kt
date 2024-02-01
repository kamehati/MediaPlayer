package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.elect.mediaplayer.EXTRA_PLAYLIST
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.db.PlaylistEntity
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.extraNotNull
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.util.PreferenceUtil
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DeletePlaylistDialog(

    private var navController: NavController? = null
): DialogFragment() {

    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    companion object {

        fun create(
            playlist: PlaylistEntity,
            navController: NavController? = null
        ): DeletePlaylistDialog {
            val list = mutableListOf<PlaylistEntity>()
            list.add(playlist)
            return create(list, navController)
        }

        fun create(
            playlists: List<PlaylistEntity>,
            navController: NavController? = null
        ): DeletePlaylistDialog {
            return DeletePlaylistDialog(navController).apply {
                arguments = bundleOf(
                    EXTRA_PLAYLIST to playlists
                )
            }
        }
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        val playlists =
            extraNotNull<List<PlaylistEntity>>(EXTRA_PLAYLIST).value

        val title: Int
        val message: CharSequence


        if (playlists.size > 1) {

            title = R.string.delete_playlists_title


            message = HtmlCompat.fromHtml(
                String.format(
                    getString(
                        R.string.delete_x_playlists
                    ),
                    playlists.size
                ),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        } else {
            title = R.string.delete_playlist_title
            message = HtmlCompat.fromHtml(
                String.format(
                    getString(
                        R.string.delete_playlist_x
                    ),
                    playlists[0].playlistName
                ),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        return materialDialog(title)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(
                android.R.string.cancel,
                null
            ).setPositiveButton(
                R.string.action_delete
            ) { _, _ ->

                libraryViewModel
                    .deleteMediasFromPlaylist(playlists)
                libraryViewModel
                    .deleteRoomPlaylist(playlists)
                libraryViewModel
                    .forceReload(ReloadType.Playlists)

                if(navController != null){
                    PreferenceUtil.appbarMode = 0
                    findNavController().navigateUp()
                }
            }
            .create()
            .colorButtons()
    }
}