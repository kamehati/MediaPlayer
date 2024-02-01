package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.EXTRA_MEDIAS
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.db.MediaEntity
import com.example.elect.mediaplayer.db.SongEntity
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.extraNotNull
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RemoveMediaFromPlaylistDialog : DialogFragment() {
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    companion object {
        fun create(mediaEntity: MediaEntity): RemoveMediaFromPlaylistDialog {
            val list = mutableListOf<MediaEntity>()
            list.add(mediaEntity)
            return create(list)
        }

        fun create(medias: List<MediaEntity>): RemoveMediaFromPlaylistDialog {
            return RemoveMediaFromPlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_MEDIAS to medias
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val medias = extraNotNull<List<MediaEntity>>(EXTRA_MEDIAS).value
        val pair = if (medias.size > 1) {
            Pair(
                R.string.remove_songs_from_playlist_title,
                HtmlCompat.fromHtml(
                    String.format(getString(R.string.remove_x_songs_from_playlist), medias.size),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
        } else {
            Pair(
                R.string.remove_song_from_playlist_title,
                HtmlCompat.fromHtml(
                    String.format(
                        getString(R.string.remove_song_x_from_playlist),
                        medias[0].title
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
        }
        return materialDialog(pair.first)
            .setMessage(pair.second)
            .setPositiveButton(R.string.remove_action) { _, _ ->
                libraryViewModel.deleteMediasInPlaylist(medias)
            }
            .setNegativeButton(
                android.R.string.cancel, null
            ).create()
            .colorButtons()
    }
}
