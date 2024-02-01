package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.EXTRA_PLAYLIST_ID
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.db.PlaylistEntity
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.extraNotNull
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.ReloadType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RenamePlaylistDialog: DialogFragment() {

    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    companion object {
        fun create(
            playlistEntity: PlaylistEntity
        ): RenamePlaylistDialog {
            return RenamePlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_PLAYLIST_ID to playlistEntity
                )
            }
        }
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        val playlistEntity =
            extraNotNull<PlaylistEntity>(EXTRA_PLAYLIST_ID).value


        val layout = LayoutInflater
            .from(requireContext())
            .inflate(
                R.layout.dialog_playlist,
                null
            )

        val inputEditText: TextInputEditText =
            layout.findViewById(R.id.actionNewPlaylist)
        val nameContainer: TextInputLayout =
            layout.findViewById(R.id.actionNewPlaylistContainer)


        inputEditText.setText(playlistEntity.playlistName)

        return materialDialog(
            R.string.rename_playlist_title
        ).setView(
            layout
        ).setNeutralButton(
            android.R.string.cancel,
            null
        ).setPositiveButton(
            R.string.action_rename
        ) { _, _ ->
            val name = inputEditText.text.toString()
            if (name.isNotEmpty()) {

                libraryViewModel.renameRoomPlaylist(
                    playlistEntity.playlistId,
                    name
                )

                libraryViewModel.forceReload(
                    ReloadType.Playlists
                )
            } else {
                nameContainer.error = "プレイリスト名を空にはできません"
            }
        }.create().colorButtons()
    }
}