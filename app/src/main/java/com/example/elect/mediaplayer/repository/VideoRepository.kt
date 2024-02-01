package com.example.elect.mediaplayer.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.example.elect.mediaplayer.Constants.baseMediaVideoProjection
import com.example.elect.mediaplayer.Constants.baseVideoFolderProjection
import com.example.elect.mediaplayer.Constants.baseVideoProjection
import com.example.elect.mediaplayer.extensions.getInt
import com.example.elect.mediaplayer.extensions.getLong
import com.example.elect.mediaplayer.extensions.getString
import com.example.elect.mediaplayer.extensions.getStringOrNull
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.model.*
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import java.io.File
import java.text.Collator
import java.util.ArrayList

interface VideoRepository {
    fun videos(): List<Media>
    fun videos(cursor: Cursor?): List<Media>
    fun videoFolders(): List<VideoFolder>
    fun videoFolders(cursor: Cursor?): List<VideoFolder>

    fun videoFolder(folderId: Long): VideoFolder
}

class RealVideoRepository (
    private val context: Context
    ): VideoRepository{

    override fun videos(): List<Media> {
        return videos(
            makeVideoCursor(null, null)
        )
    }

    override fun videos(
        cursor: Cursor?
    ): List<Media> {
        val videos = arrayListOf<Media>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                videos.add(getVideoFromCursorImpl(cursor))
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

    private fun getVideoFromCursorImpl(
        cursor: Cursor
    ): Media{
        val id = cursor.getLong(MediaStore.Video.Media._ID)
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

    override fun videoFolders(): List<VideoFolder> {
        var tmpList = videoFolders(
            makeVideoFolderCursor(null, null)
        )
        tmpList = tmpList.distinct()

        val videos = videos()

        tmpList.map { videoFolder ->
            videos.map { video ->
                if(videoFolder.id == video.folderId){
                    videoFolder.videos += video
                }
            }
        }

        return tmpList
    }

    override fun videoFolders(
        cursor: Cursor?
    ): List<VideoFolder> {
        val videoFolders = arrayListOf<VideoFolder>()
        if(cursor != null && cursor.moveToFirst()){
            do {
                videoFolders.add(
                    getVideoFolderFromCursorImpl(cursor)
                )
            } while (cursor.moveToNext())
        }

        cursor?.close()

        val collator = Collator.getInstance()

        return when(PreferenceUtil.videoFolderSortOrder) {
            SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_A_Z -> {
                videoFolders.sortedWith{s1, s2 ->
                    collator.compare(s1.folderName, s2.folderName)
                }
            }
            SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_Z_A -> {
                videoFolders.sortedWith{s1, s2 ->
                    collator.compare(s2.folderName, s1.folderName)
                }
            }
            else -> videoFolders
        }
    }

    override fun videoFolder(folderId: Long): VideoFolder {
        val cursor = makeVideoCursor(
            MediaStore.Video.Media.BUCKET_ID + "=?",
            arrayOf(folderId.toString()),
            getVideoLoaderSortOrder()
        )
        val videos = videos(cursor)

        val folderName = videos.firstOrNull()?.folderName
        val videoFolder = folderName?.let {
            VideoFolder(folderId, it, ArrayList())
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

    private fun getVideoLoaderSortOrder(): String {
        var videoSortOrder = PreferenceUtil.videoFolderDetailsSortOrder
        if (videoSortOrder == SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z)
            videoSortOrder = SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z
        return videoSortOrder + ", " +
                PreferenceUtil.videoFolderDetailsSortOrder
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

    private fun getVideoFolderFromCursorImpl(
        cursor: Cursor
    ): VideoFolder{

        val folderId = cursor.getLong(MediaStore.Video.VideoColumns.BUCKET_ID)
        val folderName = cursor.getString(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)

        return VideoFolder(
            folderId,
            folderName,
            arrayListOf()
        )
    }


    fun makeVideoFolderCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.videoFolderSortOrder
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
                baseVideoFolderProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        }catch (ex: SecurityException){
            return null
        }
    }
}