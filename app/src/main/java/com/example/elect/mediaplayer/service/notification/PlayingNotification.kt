package com.example.elect.mediaplayer.service.notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.glide.palette.BitmapPaletteWrapper
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.service.MusicService.Companion.ACTION_PREVIOUS
import com.example.elect.mediaplayer.service.MusicService.Companion.ACTION_QUIT
import com.example.elect.mediaplayer.service.MusicService.Companion.ACTION_SKIP
import com.example.elect.mediaplayer.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.example.elect.mediaplayer.service.MusicService.Companion.TOGGLE_FAVORITE
import com.example.elect.mediaplayer.util.BuildUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("RestrictedApi")
class PlayingNotification(
    val context: Context,
    mediaSessionToken: MediaSessionCompat.Token
): BasePlayingNotification(
    context
) {
    init {
        val action = Intent(context, MainActivity::class.java)
        action.putExtra(
            MainActivity.EXPAND_PANEL,
            false
        )
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

        val clickIntent =
            PendingIntent.getActivity(
                context,
                0,
                action,

                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (BuildUtil.isMarshmallowPlus())
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
            )

        setContentIntent(clickIntent)


        val intent = Intent(ACTION_QUIT)
        val serviceName = ComponentName(

            context,
            MusicService::class.java
        )
        intent.component = serviceName
        val deleteIntent = PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (BuildUtil.isMarshmallowPlus())
                        PendingIntent.FLAG_IMMUTABLE
                    else 0)
        )

        setDeleteIntent(deleteIntent)



        val playPauseAction = buildPlayAction(true)

        val previousAction = NotificationCompat.Action(
            R.drawable.ic_round_skip_previous_large,
            context.getString(R.string.action_previous),

            retrievePlaybackAction(ACTION_PREVIOUS)
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_round_skip_next_large,
            context.getString(R.string.action_next),

            retrievePlaybackAction(ACTION_SKIP)
        )

        val dismissAction = NotificationCompat.Action(
            R.drawable.ic_round_close_large,
            context.getString(R.string.action_cancel),

            retrievePlaybackAction(ACTION_QUIT)
        )

        setSmallIcon(R.drawable.ic_round_music_note)

        setShowWhen(false)

        addAction(previousAction)
        addAction(playPauseAction)
        addAction(nextAction)
        addAction(dismissAction)

        setStyle(

            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionToken)

                .setShowActionsInCompactView(0, 1, 2)
        )

        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O

        ) {

            this.color = color
        }
    }

    private fun buildFavoriteAction(isFavorite: Boolean)
            : NotificationCompat.Action {
        val favoriteResId =
            if (isFavorite) R.drawable.ic_round_favorite
            else R.drawable.ic_round_favorite_border

        return NotificationCompat.Action.Builder(
            favoriteResId,
            context.getString(R.string.action_toggle_favorite),
            retrievePlaybackAction(TOGGLE_FAVORITE)
        ).build()
    }


    private fun buildPlayAction(
        isPlaying: Boolean
    ): NotificationCompat.Action {
        val playButtonResId =
            if (isPlaying) R.drawable.ic_round_pause_large
            else R.drawable.ic_round_play_arrow_large
        return NotificationCompat.Action.Builder(
            playButtonResId,
            context.getString(R.string.action_play_pause),

            retrievePlaybackAction(ACTION_TOGGLE_PAUSE)
        ).build()
    }


    private fun retrievePlaybackAction(
        action: String
    ): PendingIntent {

        val serviceName = ComponentName(
            context,
            MusicService::class.java
        )
        val intent = Intent(action)
        intent.component = serviceName

        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (BuildUtil.isMarshmallowPlus())
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )
    }

    override fun updateMetadata(
        media: Media,
        onUpdate: () -> Unit
    ) {

        setContentTitle(

            HtmlCompat.fromHtml(

                "<b>" + media.title + "</b>",

                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        setContentText(media.artistName)

        setSubText(
            HtmlCompat.fromHtml(
                "<b>" + media.albumName + "</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        val bigNotificationImageSize = context.resources
            .getDimensionPixelSize(R.dimen.notification_big_image_size)

        GlideApp.with(context)
            .asBitmapPalette()
            .mediaCoverOptions(media)
            .load(
                GlideExtensions.getMediaModel(media)
            ).centerCrop()
            .into(
                object : CustomTarget<BitmapPaletteWrapper>(
                    bigNotificationImageSize,
                    bigNotificationImageSize
                ) {

                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?
                    ) {

                        setLargeIcon(
                            resource.getBitmap()
                        )

                        if (BuildUtil.isOreoPlus()

                        ) {

                        }

                        onUpdate()
                    }


                    override fun onLoadFailed(
                        errorDrawable: Drawable?
                    ) {
                        super.onLoadFailed(errorDrawable)

                        setLargeIcon(
                            BitmapFactory.decodeResource(
                                context.resources,
                                R.drawable.ic_music_large
                            )
                        )

                        onUpdate()
                    }


                    override fun onLoadCleared(
                        placeholder: Drawable?
                    ) {

                        setLargeIcon(
                            BitmapFactory.decodeResource(
                                context.resources,
                                R.drawable.ic_music_large
                            )
                        )

                        onUpdate()
                    }
                }
            )

        updateFavorite(media, onUpdate)
    }

    override fun setPlaying(
        isPlaying: Boolean,
        onUpdate: () -> Unit
    ) {

        mActions[1] = buildPlayAction(isPlaying)

        onUpdate()
    }

    override fun updateFavorite(
        media: Media,
        onUpdate: () -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {

                onUpdate()
            }
        }
    }

    companion object {
        fun from(
            context: Context,
            notificationManager: NotificationManager,
            mediaSession: MediaSessionCompat
        ): PlayingNotification {

            if (BuildUtil.isOreoPlus()) {

                createNotificationChannel(
                    context, notificationManager)
            }
            return PlayingNotification(
                context,
                mediaSession.sessionToken
            )
        }
    }
}