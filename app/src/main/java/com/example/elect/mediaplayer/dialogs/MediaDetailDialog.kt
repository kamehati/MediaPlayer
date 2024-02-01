package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.EXTRA_MEDIAS
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.MusicUtil
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.io.IOException

class MediaDetailDialog: DialogFragment() {

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val context = requireContext()
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_file_details, null)

        val media = requireArguments().getParcelable<Media>(EXTRA_MEDIAS)
        val mediaTitle: TextView = dialogView.findViewById(R.id.mediaTitle)
        val fileName: TextView = dialogView.findViewById(R.id.fileName)
        val filePath: TextView = dialogView.findViewById(R.id.filePath)
        val fileSize: TextView = dialogView.findViewById(R.id.fileSize)
        val dateModified: TextView = dialogView.findViewById(R.id.dateModified)
        val fileFormat: TextView = dialogView.findViewById(R.id.fileFormat)
        val trackLength: TextView = dialogView.findViewById(R.id.trackLength)
        val bitRate: TextView = dialogView.findViewById(R.id.bitrate)
        val samplingRate: TextView = dialogView.findViewById(R.id.samplingRate)

        mediaTitle.text = makeTextWithTitle(context, R.string.title_name, "-")
        fileName.text = makeTextWithTitle(context, R.string.label_file_name, "-")
        filePath.text = makeTextWithTitle(context, R.string.label_file_path, "-")
        fileSize.text = makeTextWithTitle(context, R.string.label_file_size, "-")

        fileFormat.text = makeTextWithTitle(context, R.string.label_file_format, "-")
        trackLength.text = makeTextWithTitle(context, R.string.label_track_length, "-")
        bitRate.text = makeTextWithTitle(context, R.string.label_bit_rate, "-")
        samplingRate.text = makeTextWithTitle(context, R.string.label_sampling_rate, "-")

        if(media != null){
            val mediaFile = File(media.data)

            if(mediaFile.exists()){
                mediaTitle.text = makeTextWithTitle(
                    context,
                    R.string.title_name,
                    media.title
                )

                fileName.text = makeTextWithTitle(
                    context,
                    R.string.label_file_name,
                    mediaFile.name
                )

                filePath.text = makeTextWithTitle(
                    context,
                    R.string.label_file_path,
                    mediaFile.absolutePath
                )

                dateModified.text = makeTextWithTitle(
                    context,
                    R.string.label_last_modified,
                    MusicUtil.getDateModifiedString(
                        mediaFile.lastModified()
                    )
                )

                fileSize.text =
                    makeTextWithTitle(
                        context,
                        R.string.label_file_size,
                        getFileSizeString(mediaFile.length())
                    )

                try {
                    if(media.isSongOrVideo == 1){
                        val audioFile = AudioFileIO.read(mediaFile)
                        val audioHeader = audioFile.audioHeader

                        fileFormat.text = makeTextWithTitle(
                            context,
                            R.string.label_file_format,
                            audioHeader.format
                        )

                        trackLength.text = makeTextWithTitle(
                            context,
                            R.string.label_track_length,
                            MusicUtil.getReadableDurationString(
                                (audioHeader.trackLength * 1000).toLong()
                            )
                        )
                        bitRate.text = makeTextWithTitle(
                            context,
                            R.string.label_bit_rate,
                            audioHeader.bitRate + " kb/s"
                        )
                        samplingRate.text =
                            makeTextWithTitle(
                                context,
                                R.string.label_sampling_rate,
                                audioHeader.sampleRate + " Hz"
                            )
                    } else if(media.isSongOrVideo == 2){
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(media.data)

                        fileFormat.text = makeTextWithTitle(
                            context,
                            R.string.label_file_format,
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_MIMETYPE
                            )
                        )

                        trackLength.text = makeTextWithTitle(
                            context,
                            R.string.label_track_length,
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_DURATION
                            )?.let {
                                MusicUtil.getReadableDurationString(
                                    it.toLong()
                                )
                            }
                        )

                        bitRate.text = makeTextWithTitle(
                            context,
                            R.string.label_bit_rate,
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_BITRATE
                            ) + " kb/s"
                        )
                    }
                } catch (@NonNull e: IOException){
                    trackLength.text = makeTextWithTitle(
                        context,
                        R.string.label_track_length,
                        MusicUtil.getReadableDurationString(
                            media.duration
                        )
                    )
                }
            }
            else{
                fileName.text = makeTextWithTitle(
                    context,
                    R.string.label_file_name,
                    media.title
                )
                trackLength.text = makeTextWithTitle(
                    context,
                    R.string.label_track_length,
                    MusicUtil.getReadableDurationString(
                        media.duration
                    )
                )
            }
        }

        return materialDialog(R.string.action_details)
            .setPositiveButton(android.R.string.ok, null)
            .setView(dialogView)
            .create()
            .colorButtons()
    }

    companion object {
        fun create(media: Media): MediaDetailDialog{
            return MediaDetailDialog().apply {
                arguments = bundleOf(
                    EXTRA_MEDIAS to media
                )
            }
        }

        private fun makeTextWithTitle(
            context: Context,
            titleResId: Int,
            text: String?
        ): Spanned{
            return HtmlCompat.fromHtml(
                "<b>" +
                        context.resources.getString(titleResId) +
                        ": " +
                        "</b>" +
                        text,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        private fun getFileSizeString(
            sizeInBytes: Long
        ): String{
            val fileSizeInKB = sizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            return "$fileSizeInMB MB"
        }
    }
}