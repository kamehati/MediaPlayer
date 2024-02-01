package com.example.elect.mediaplayer.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import com.example.elect.mediaplayer.Constants
import com.example.elect.mediaplayer.Constants.baseMediaVideoProjection
import com.example.elect.mediaplayer.extensions.getInt
import com.example.elect.mediaplayer.extensions.getLong
import com.example.elect.mediaplayer.extensions.getString
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Video
import com.example.elect.mediaplayer.model.VideoFolder
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import java.io.File
import java.text.Collator
import java.util.ArrayList

interface MediaRepository {
    fun videoFolder(folderId: Long): VideoFolder
    fun videos(cursor: Cursor?): List<Media>
}

class RealMediaRepository(
    private val context: Context
) : MediaRepository {

    override fun videoFolder(folderId: Long): VideoFolder {
        val cursor = makeVideoCursor(
            MediaStore.Video.Media.BUCKET_ID + "=?",
            arrayOf(folderId.toString()),
            getVideoLoaderSortOrder()
        )
        val videos = videos(cursor)

        val folderName = videos.firstOrNull()?.folderName
        val videoFolder = folderName?.let {
            VideoFolder(folderId, it, videos)
        }
        return if(videoFolder != null){

            sortFolderVideos(videoFolder)
        } else {
            VideoFolder(
                -1,
                "",
                ArrayList()
            )
        }
    }

    fun makeVideoCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.videoSortOrder
    ): Cursor?{
        val selectionFinal = selection
        val selectionValuesFinal = selectionValues

        val uri = if(BuildUtil.isQPlus()){
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return try {
            context.contentResolver.query(
                uri,
                baseMediaVideoProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException){
            return null
        }
    }

    private fun getVideoLoaderSortOrder(): String {
        var videoSortOrder = PreferenceUtil.videoFolderDetailsSortOrder
        if (videoSortOrder == SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z)
            videoSortOrder = SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z
        return videoSortOrder + ", " +
                PreferenceUtil.videoFolderDetailsSortOrder
    }


    override fun videos(cursor: Cursor?): List<Media> {
        val videos = arrayListOf<Media>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                videos.add(getMediaVideoFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }

        cursor?.close()

        val collator = Collator.getInstance()

        return when(PreferenceUtil.videoSortOrder) {
            SortOrder.VideoSortOrder.VIDEO_A_Z -> {
                videos.sortedWith{s1, s2 ->
                    collator.compare(s1.title, s2.title)
                }
            }
            SortOrder.VideoSortOrder.VIDEO_Z_A -> {
                videos.sortedWith{s1, s2 ->
                    collator.compare(s2.title, s1.title)
                }
            }
            else -> videos
        }
    }

    private fun getMediaVideoFromCursorImpl(
        cursor: Cursor
    ): Media{
        val id = cursor.getLong(BaseColumns._ID)
        val title = cursor.getString(MediaStore.Video.Media.TITLE)
        val year = cursor.getInt(MediaStore.Video.Media.YEAR)
        val duration = cursor.getLong(MediaStore.Video.VideoColumns.DURATION)
        val data = cursor.getString(MediaStore.Video.VideoColumns.DATA)
        val dateModified = cursor.getLong(MediaStore.Video.VideoColumns.DATE_MODIFIED)
        val folderId = cursor.getLong(MediaStore.Video.VideoColumns.BUCKET_ID)
        val folderName = cursor.getString(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)
        val videoId = cursor.getLong(MediaStore.Video.VideoColumns._ID)
        val size = cursor.getString(MediaStore.Video.VideoColumns.SIZE)

        return Media(
            id,
            title,
            year,
            duration,
            data,
            dateModified,
            folderId,
            folderName,
            -1,
            -1,
            -1,
            "",
            -1,
            "",
            "",
            "",
            videoId,
            size,
            2
        )
    }

    private fun sortFolderVideos(
        videoFolder: VideoFolder
    ): VideoFolder {
        val collator = Collator.getInstance()
        val videos = when (PreferenceUtil.videoFolderDetailsSortOrder) {
            SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z -> {
                videoFolder.videos.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }
            SortOrder.VideoFolderDetailsSortOrder.VIDEO_Z_A -> {
                videoFolder.videos.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }
            else -> throw IllegalArgumentException("invalid ${PreferenceUtil.videoFolderDetailsSortOrder}")
        }

        return videoFolder.copy(videos = videos)
    }
}