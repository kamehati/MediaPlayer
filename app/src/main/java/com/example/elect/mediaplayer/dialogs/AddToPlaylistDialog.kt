package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.*
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.db.PlaylistEntity
import com.example.elect.mediaplayer.db.toMediaEntity
import com.example.elect.mediaplayer.db.toSongEntity
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.model.Media
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import com.example.elect.mediaplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddToPlaylistDialog(

    private val activity: MainActivity
    ): DialogFragment() {

    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    companion object {

        fun create(
            playlistEntities: List<PlaylistEntity>,
            media: Media,
            activity: MainActivity
        ): AddToPlaylistDialog {
            val list: MutableList<Media> = mutableListOf()
            list.add(media)
            return create(playlistEntities, list, activity)
        }


        fun create(
            playlistEntities: List<PlaylistEntity>,
            medias: List<Media>,
            activity: MainActivity
        ): AddToPlaylistDialog {
            return AddToPlaylistDialog(activity).apply {

                arguments = bundleOf(
                    EXTRA_MEDIAS to medias,
                    EXTRA_PLAYLISTS to playlistEntities
                )
            }
        }
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        val playlistEntities =

        extraNotNull<List<PlaylistEntity>>(EXTRA_PLAYLISTS).value

        val medias = extraNotNull<List<Media>>(EXTRA_MEDIAS).value

        val playlistsNames = mutableListOf<String>()

        playlistsNames.add(
            requireContext()
                .resources
                .getString(
                    R.string.action_new_playlist
                )
        )


        for (entity in playlistEntities){

            playlistsNames.add(entity.playlistName)
        }

        return materialDialog(
            R.string.add_playlist_title
        ).setAdapter(
            playlistAdapter(playlistsNames)
        ){ dialog, which ->
            if(which == 0){
                showCreateDialog(medias)
            }
            else {
                lifecycleScope.launch(Dispatchers.IO){

                    val mediaEntities = medias
                        .toMediaEntity(playlistEntities[which - 1])


                    libraryViewModel.insertMedias(mediaEntities)

                    libraryViewModel.forceReload(ReloadType.Playlists)


                    make(
                        activity.getSnackBar(),
                        setString(playlistsNames[which], mediaEntities.size),
                        Snackbar.LENGTH_SHORT
                    ).backgroundColor(
                        R.color.md_blue_grey_600
                    ).textColor(
                        R.color.md_white_1000
                    ).show()
                }
            }
            dialog.dismiss()
        }
            .create()

            .colorButtons()
    }

    private fun setString(
        playlistName: String,
        size: Int
    ): String {

        return App.getContext().getString(
            R.string.added_song_count_to_playlist,
            size,
            playlistName
        )
    }

    private fun playlistAdapter(
        playlists: List<String>
    ): ArrayAdapter<String> {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.item_simple_text,
            R.id.title
        )

        adapter.addAll(playlists)

        return adapter
    }

    private fun showCreateDialog(medias: List<Media>){
        CreatePlaylistDialog
            .create(
                medias,
                activity
            )
            .show(
                requireActivity().
                supportFragmentManager,
                "Dialog"
            )
    }
}