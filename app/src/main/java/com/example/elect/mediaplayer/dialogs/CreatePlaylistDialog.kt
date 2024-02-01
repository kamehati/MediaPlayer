package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.App
import com.example.elect.mediaplayer.EXTRA_MEDIAS
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.DialogPlaylistBinding
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CreatePlaylistDialog(
    private val activity: MainActivity
): DialogFragment() {

    private var _binding: DialogPlaylistBinding? = null
    private val binding get() = _binding!!
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    companion object {

        fun create(
            media: Media,
            activity: MainActivity
        ): CreatePlaylistDialog {
            val list = mutableListOf<Media>()
            list.add(media)
            return create(list, activity)
        }


        fun create(
            medias: List<Media>,
            activity: MainActivity
        ): CreatePlaylistDialog {
            return CreatePlaylistDialog(activity).apply {
                arguments = bundleOf(
                    EXTRA_MEDIAS to medias
                )
            }
        }
    }


    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        _binding = DialogPlaylistBinding
            .inflate(layoutInflater)

        val medias: List<Media> =
            extra<List<Media>>(EXTRA_MEDIAS).value ?: emptyList()
        val playlistView: TextInputEditText = binding.actionNewPlaylist
        val playlistContainer: TextInputLayout = binding.actionNewPlaylistContainer

        return materialDialog(R.string.new_playlist_title)
            .setView(binding.root)
            .setPositiveButton(
                R.string.create_action
            ) { _, _ ->
                val playlistName = playlistView.text.toString()

                if (!TextUtils.isEmpty(playlistName)) {

                    libraryViewModel.addMediaToPlaylist(playlistName, medias)


                    make(
                        activity.getSnackBar(),
                        setString(playlistName),
                        Snackbar.LENGTH_SHORT
                    ).backgroundColor(
                        R.color.md_blue_grey_600
                    ).textColor(
                        R.color.md_white_1000
                    ).show()
                } else {

                    playlistContainer.error =
                        "プレイリスト名を空にはできません"
                }
            }
            .create()
            .colorButtons()
    }

    private fun setString(
        playlistName: String
    ): String {

        return App.getContext()
            .getString(
                R.string.playlist_created_sucessfully,
                playlistName
            )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}