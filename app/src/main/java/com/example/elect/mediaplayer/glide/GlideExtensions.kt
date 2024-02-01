package com.example.elect.mediaplayer.glide

import android.net.Uri
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.annotation.GlideType
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.MediaStoreSignature
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.glide.audiocover.AudioFileCover
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteWrapper
import com.example.elect.mediaplayer.model.*
import com.example.elect.mediaplayer.util.MusicUtil
import java.io.File

@GlideExtension
object GlideExtensions {

    private const val DEFAULT_SONG_IMAGE: Int = R.drawable.ic_round_music_note
    private const val DEFAULT_ALBUM_IMAGE: Int = R.drawable.ic_round_album
    private const val DEFAULT_ARTIST_IMAGE: Int = R.drawable.ic_round_remember_me
    private const val DEFAULT_PLAYLIST_IMAGE: Int = R.drawable.ic_round_playlist_play
    private const val DEFAULT_VIDEO_IMAGE: Int = R.drawable.ic_round_music_video
    const val DEFAULT_FOLDER_IMAGE: Int = R.drawable.ic_round_folder
    const val DEFAULT_FAVORITE_TRUE: Int = R.drawable.ic_round_favorite
    const val DEFAULT_FAVORITE_FALSE: Int = R.drawable.ic_round_favorite_border

    private val DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.NONE

    @JvmStatic
    @GlideType(BitmapPaletteWrapper::class)
    fun asBitmapPalette(
        requestBuilder: RequestBuilder<BitmapPaletteWrapper>
    ): RequestBuilder<BitmapPaletteWrapper> {
        return requestBuilder
    }

    @JvmStatic
    @GlideOption
    fun songCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        song: Song
    ): BaseRequestOptions<*>{
        return baseRequestOptions
            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(DEFAULT_SONG_IMAGE)
            .placeholder(DEFAULT_SONG_IMAGE)
            .signature(createSignature(song))
    }

    @JvmStatic
    @GlideOption
    fun mediaSongCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        media: Media
    ): BaseRequestOptions<*>{
        return baseRequestOptions
            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(DEFAULT_SONG_IMAGE)
            .placeholder(DEFAULT_SONG_IMAGE)
            .signature(createSignature(media))
    }

    @JvmStatic
    @GlideOption
    fun mediaCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        media: Media
    ): BaseRequestOptions<*>{
        when(media.isSongOrVideo){
            1 -> {
                return baseRequestOptions
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_SONG_IMAGE)
                    .placeholder(DEFAULT_SONG_IMAGE)
                    .signature(createSignature(media))
            }
            2 -> {
                return baseRequestOptions
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_VIDEO_IMAGE)
                    .placeholder(DEFAULT_VIDEO_IMAGE)
                    .signature(createSignature(media))
            }
            else -> {
                return baseRequestOptions
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_SONG_IMAGE)
                    .placeholder(DEFAULT_SONG_IMAGE)
                    .signature(createSignature(media))
            }
        }
    }

    @JvmStatic
    @GlideOption
    fun videoCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        media: Media
    ): BaseRequestOptions<*>{
        return baseRequestOptions
            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(DEFAULT_FOLDER_IMAGE)
            .placeholder(DEFAULT_FOLDER_IMAGE)
            .signature(createSignature(media))
    }


    private fun createSignature(song: Song)
    : com.bumptech.glide.load.Key{

        return MediaStoreSignature(

            "",

            song.dateModified,

            0
        )
    }

    private fun createSignature(media: Media)
    : com.bumptech.glide.load.Key{

        return MediaStoreSignature(

            "",

            media.dateModified,

            0
        )
    }

    fun getSongModel(song: Song): Any{
        return AudioFileCover(song.data)

    }

    fun getAlbumModel(song: Song): Any{
        return MusicUtil.getMediaStoreAlbumCoverUri(song.albumId)
    }

    fun getMediaModel(media: Media): Any{
        return when(media.isSongOrVideo){
            1 -> AudioFileCover(media.data)
            2 -> {
                val file = File(media.data)
                Uri.fromFile(file)
            }
            else -> AudioFileCover(media.data)
        }
    }

    @JvmStatic
    @GlideOption
    fun folderCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>
    ): BaseRequestOptions<*>{
        return baseRequestOptions
            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .error(DEFAULT_FOLDER_IMAGE)
            .placeholder(DEFAULT_FOLDER_IMAGE)
    }

    @JvmStatic
    @GlideOption
    fun albumCoverOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        media: Media
    ): BaseRequestOptions<*> {
        return baseRequestOptions

            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

            .error(DEFAULT_ALBUM_IMAGE)

            .placeholder(DEFAULT_ALBUM_IMAGE)

            .signature(

                createSignature(media)
            )
    }

    @JvmStatic
    @GlideOption
    fun artistImageOptions(
        baseRequestOptions: BaseRequestOptions<*>,
        media: Media
    ): BaseRequestOptions<*> {
        return baseRequestOptions

            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

            .priority(Priority.LOW)

            .error(DEFAULT_ARTIST_IMAGE)

            .placeholder(DEFAULT_ARTIST_IMAGE)

            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

            .signature(createSignature(media))
    }


    @JvmStatic
    @GlideOption
    fun playlistOptions(
        baseRequestOptions: BaseRequestOptions<*>
    ): BaseRequestOptions<*> {
        return baseRequestOptions

            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

            .error(DEFAULT_PLAYLIST_IMAGE)
            .placeholder(DEFAULT_PLAYLIST_IMAGE)
    }

    fun getPlaylistModel(): Any{
        return DEFAULT_PLAYLIST_IMAGE
    }
}