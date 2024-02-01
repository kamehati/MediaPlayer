package com.example.elect.mediaplayer.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.media.AudioAttributesCompat
import androidx.media.AudioAttributesCompat.CONTENT_TYPE_MUSIC
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.preference.PreferenceManager
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.SAVED_POSITION
import com.example.elect.mediaplayer.SAVED_POSITION_IN_TRACK
import com.example.elect.mediaplayer.auto.AutoMusicProvider
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Media.Companion.emptyMedia
import com.example.elect.mediaplayer.service.notification.BasePlayingNotification
import com.example.elect.mediaplayer.service.notification.PlayingNotification.Companion.from
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.MusicUtil.getMediaFileUri
import com.example.elect.mediaplayer.util.MusicUtil.getMediaStoreAlbumCoverUri
import com.example.elect.mediaplayer.util.MusicUtil.isFavorite
import com.example.elect.mediaplayer.util.MusicUtil.toggleFavorite
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.example.elect.mediaplayer.volume.AudioVolumeObserver
import com.example.elect.mediaplayer.volume.OnAudioVolumeChangedListener
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent
import java.util.*


class MusicService:
    MediaBrowserServiceCompat(),
    Playback.PlaybackCallbacks
{


    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder


    var playback: Playback? = null
    private var musicPlayerHandlerThread: HandlerThread? = null
    private var playerHandler: PlaybackHandler? = null

    private var throttledSeekHandler: ThrottledSeekHandler? = null
    private var queueSaveHandler: QueueSaveHandler? = null
    private var queueSaveHandlerThread: HandlerThread? = null
    private var queuesRestored = false

    private var notHandledMetaChangedForCurrentTrack = false
    private var originalPlayingQueue = mutableListOf<Media>()
    var playingQueue = mutableListOf<Media>()

    private var playingNotification: BasePlayingNotification? = null
    private var notificationManager: NotificationManager? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private val mMusicProvider =
        KoinJavaComponent.get<AutoMusicProvider>(AutoMusicProvider::class.java)

    private var isForeground = false

    val audioSessionId: Int
        get() = if(playback != null){
        playback!!.audioSessionId
    } else {
        -1
    }

    @JvmField
    var position = -1
    @JvmField
    var nextPosition = -1

    var isPausedByTransientLossOfFocus = false

    val currentMedia: Media
        get() = getMediaAt(getPosition())

    val nextMedia: Media?

        get() = if (
            isLastTrack &&
            repeatMode == REPEAT_MODE_NONE
        ) {
            null
        } else {

            getMediaAt(getNextPosition())
        }


    val mediaDurationMillis: Long
        get() = if (playback != null) {
            playback!!.duration()
        } else -1

    val mediaProgressMillis: Long
        get() = if (playback != null) {
            playback!!.position()
        } else -1


    val isLastTrack: Boolean
        get() = getPosition() == playingQueue.size - 1
    val isPlaying: Boolean
        get() = playback != null && playback!!.isPlaying

    private var audioManager: AudioManager? = null
        get() {
            if (field == null) {
                field = getSystemService()
            }
            return field
        }

    private var audioFocusRequestCompat: AudioFocusRequestCompat? = null


    private val audioFocusListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->

        }


    private val bluetoothReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context, intent: Intent
            ) {
                val action = intent.action
                if (action != null) {
                    if (
                        BluetoothDevice
                            .ACTION_ACL_CONNECTED == action
                    ) {
                        play()
                    }
                }
            }
        }

    private val bluetoothConnectedIntentFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
    private var bluetoothConnectedRegistered = false


    private val becomingNoisyReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                if (intent.action != null &&
                    intent.action ==
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY
                ) {
                    pause()
                }
            }
        }


    private val becomingNoisyReceiverIntentFilter =
        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private var becomingNoisyReceiverRegistered = false

    private val updateFavoriteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playingNotification?.updateFavorite(currentMedia) { startForegroundOrNotify() }
            startForegroundOrNotify()
        }
    }

    var repeatMode = 0
        private set(value) {
            when (value) {
                REPEAT_MODE_NONE,
                REPEAT_MODE_ALL,  -> {

                    field = value
                    PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .edit {
                            putInt(SAVED_REPEAT_MODE, value)
                        }

                    prepareNext()

                    sendBroadcast(Intent(REPEAT_MODE_CHANGED))
                }
            }
        }

    @JvmField
    var shuffleMode = 0

    private fun getMediaAt(position: Int): Media {
        return if (
            (position >= 0) &&
            (position < playingQueue.size)
        ) {
            playingQueue[position]
        } else {
            emptyMedia
        }
    }


    private fun getPosition(): Int {
        return position
    }

    private fun getNextPosition(): Int {
        var position = getPosition() + 1

        when (repeatMode) {

            REPEAT_MODE_ALL -> if (isLastTrack) {

                position = 0
            }


            REPEAT_MODE_NONE -> if (isLastTrack) {

                position -= 1
            }

            else -> if (isLastTrack) {

                position -= 1
            }
        }
        return position
    }


    private fun prepareNext() {
        playerHandler?.removeMessages(PREPARE_NEXT)
        playerHandler?.obtainMessage(PREPARE_NEXT)?.sendToTarget()
    }


    override fun onCreate() {
        super.onCreate()

        if (bluetoothConnectedRegistered) {
            unregisterReceiver(bluetoothReceiver)
            bluetoothConnectedRegistered = false
        }

        val powerManager = getSystemService<PowerManager>()
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                javaClass.name
            )
        }
        wakeLock?.setReferenceCounted(false)


        musicPlayerHandlerThread = HandlerThread("PlaybackHandler")
        musicPlayerHandlerThread?.start()

        playerHandler = PlaybackHandler(this, musicPlayerHandlerThread!!.looper)

        playback = MultiPlayer(this)
        playback?.setCallbacks(this)

        setupMediaSession()


        queueSaveHandlerThread = HandlerThread(
            "QueueSaveHandler",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        queueSaveHandlerThread?.start()
        queueSaveHandler = QueueSaveHandler(
            this,
            queueSaveHandlerThread!!.looper
        )

        registerReceiver(
            updateFavoriteReceiver,
            IntentFilter(FAVORITE_STATE_CHANGED)
        )


        sessionToken = mediaSession?.sessionToken


        notificationManager = getSystemService()

        initNotification()
        throttledSeekHandler = ThrottledSeekHandler(this, playerHandler!!)

        restoreState()

        registerBluetoothConnected()

        mMusicProvider.setMusicService(this)
    }


    private fun initNotification() {
        playingNotification =

            from(
                this,
                notificationManager!!,
                mediaSession!!
            )
    }


    override fun onDestroy() {
        unregisterReceiver(updateFavoriteReceiver)
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver)
            becomingNoisyReceiverRegistered = false
        }
        if (bluetoothConnectedRegistered) {
            unregisterReceiver(bluetoothReceiver)
            bluetoothConnectedRegistered = false
        }

        mediaSession?.isActive = false
        quit()
        releaseResources()
        wakeLock?.release()
    }

    private fun releaseResources() {
        playerHandler?.removeCallbacksAndMessages(null)
        musicPlayerHandlerThread?.quitSafely()
        queueSaveHandler?.removeCallbacksAndMessages(null)
        queueSaveHandlerThread?.quitSafely()
        if (playback != null) {
            playback?.release()
        }
        playback = null
        mediaSession?.release()
    }

    private fun registerBluetoothConnected() {

        if (!bluetoothConnectedRegistered) {
            registerReceiver(
                bluetoothReceiver,
                bluetoothConnectedIntentFilter
            )
            bluetoothConnectedRegistered = true
        }
    }

    private val musicBind: IBinder = MusicBinder()


    override fun onBind(intent: Intent): IBinder {
        return if ("android.media.browse.MediaBrowserService" == intent.action) {
            super.onBind(intent)!!
        } else musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (!isPlaying) {
            stopSelf()
        }
        return true
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        var isRecentRequest = false
        if (rootHints != null) {
            isRecentRequest = rootHints.getBoolean(
                BrowserRoot.EXTRA_RECENT
            )
        }

        val browserRootPath = if (isRecentRequest) {
            RECENT_ROOT
        } else {
            MEDIA_ID_ROOT
        }

        return BrowserRoot(browserRootPath, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == RECENT_ROOT) {
            val media = currentMedia
            val mediaItem = MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat
                    .Builder()
                    .setMediaId(media.id.toString())
                    .setTitle(media.title)
                    .setSubtitle(media.artistName)
                    .setIconUri(getMediaStoreAlbumCoverUri(media.albumId)
                    ).build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )

            result.sendResult(listOf(mediaItem))
        } else {

            result.sendResult(
                mMusicProvider.getChildren(
                    parentId,
                    resources
                )
            )
        }
    }


    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (
            intent != null &&
            intent.action != null
        ) {
            restoreQueuesAndPositionIfNecessary()

            when (intent.action) {
                ACTION_TOGGLE_PAUSE -> if (isPlaying) {
                    pause()
                } else {
                    play()
                }
                ACTION_PAUSE -> pause()
                ACTION_PLAY -> play()
                ACTION_PLAY_PLAYLIST -> playFromPlaylist(intent)
                ACTION_PREVIOUS -> back()
                ACTION_SKIP -> playNextMedia()
                ACTION_STOP -> quit()
                ACTION_QUIT -> {
                    quit()
                    clearQueue()
                }
                TOGGLE_FAVORITE -> toggleFavorite(applicationContext, currentMedia)
            }
        }
        return START_NOT_STICKY
    }


    @Synchronized
    fun restoreQueuesAndPositionIfNecessary() {

    }

    private fun playFromPlaylist(intent: Intent) {

    }

    override fun onMediaWentToNext() {
        playerHandler?.sendEmptyMessage(MEDIA_WENT_TO_NEXT)
    }

    override fun onMediaEnded() {

        acquireWakeLock(30000)
        playerHandler?.sendEmptyMessage(MEDIA_ENDED)
    }

    private fun acquireWakeLock(milli: Long) {

        wakeLock?.acquire(milli)
    }

    fun releaseWakeLock() {
        if(wakeLock!!.isHeld){
            wakeLock?.release()
        }
    }

    fun openQueue(
        playingQueue: List<Media>?,
        startPosition: Int,
        startPlaying: Boolean
    ) {

        if (playingQueue != null &&
            playingQueue.isNotEmpty() &&
            startPosition >= 0 &&
            startPosition < playingQueue.size
        ) {

            originalPlayingQueue = ArrayList(playingQueue)
            this.playingQueue = ArrayList(originalPlayingQueue)
            var position = startPosition

            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {

                makeShuffleList(
                    this.playingQueue,
                    startPosition)
                position = 0
            }


            if (startPlaying) {

                playMediaAt(position)
            } else {

                setPosition(position)
            }

            handleAndSendChangeInternal(QUEUE_CHANGED)
        }
    }

    private fun setPosition(position: Int) {
        playerHandler?.removeMessages(SET_POSITION)

        playerHandler?.obtainMessage(
            SET_POSITION,
            position,
            0
        )?.sendToTarget()
    }


    fun toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE)
        }
        else {
            setShuffleMode(SHUFFLE_MODE_NONE)
        }
    }

    private fun setShuffleMode(shuffleMode: Int) {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putInt(SAVED_SHUFFLE_MODE, shuffleMode)
            .apply()

        when (shuffleMode) {
            SHUFFLE_MODE_SHUFFLE -> {
                this.shuffleMode = shuffleMode
                makeShuffleList(playingQueue, getPosition())
                position = 0
            }

            SHUFFLE_MODE_NONE -> {

            this.shuffleMode = shuffleMode
            val currentMediaId =
                Objects.requireNonNull(currentMedia).id
            playingQueue = ArrayList(originalPlayingQueue)

            var newPosition = 0
            for (media in playingQueue) {
                if (media.id == currentMediaId) {
                    newPosition = playingQueue.indexOf(media)
                }
            }
            position = newPosition
            }
        }


        sendBroadcast(Intent(SHUFFLE_MODE_CHANGED))
        handleAndSendChangeInternal(QUEUE_CHANGED)
    }

    fun makeShuffleList(
        listToShuffle: MutableList<Media>,

        current: Int
    ) {

        if (listToShuffle.isEmpty()) return

        if (current >= 0) {

            val media = listToShuffle.removeAt(current)

            listToShuffle.shuffle()

            listToShuffle.add(0, media)
        } else {
            listToShuffle.shuffle()
        }
    }


    fun cycleRepeatMode() {
        repeatMode = when (repeatMode) {
            REPEAT_MODE_NONE -> REPEAT_MODE_ALL
            REPEAT_MODE_ALL -> REPEAT_MODE_NONE
            else -> REPEAT_MODE_NONE
        }
    }


    fun back() {

        if (mediaProgressMillis > 2000) {

            seek(0)
        } else {

            playPreviousMedia()
        }
    }

    private fun playPreviousMedia() {

        playMediaAt(

            getPreviousPosition()
        )
    }

    private fun getPreviousPosition(): Int {

        var newPosition = getPosition() - 1
        when (repeatMode) {

            REPEAT_MODE_ALL -> if (newPosition < 0) {

                newPosition = playingQueue.size - 1
            }


            REPEAT_MODE_NONE -> if (newPosition < 0) {
                newPosition = 0
            }

            else -> if (newPosition < 0) {
                newPosition = 0
            }
        }
        return newPosition
    }


    fun playNextMedia() {

        playMediaAt(

            getNextPosition()
        )
    }

    private lateinit var loudnessEnhancer: LoudnessEnhancer

    fun play(){
        synchronized(this) {

                if(
                    playback != null &&
                    !playback!!.isPlaying
                ) {
                    if(!playback!!.isInitialized){
                        playMediaAt(
                            getPosition()
                        )
                    }
                    else {
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(
                                becomingNoisyReceiver,
                                becomingNoisyReceiverIntentFilter
                            )
                            becomingNoisyReceiverRegistered = true
                        }


                        if (notHandledMetaChangedForCurrentTrack) {

                            handleChangeInternal(META_CHANGED)
                            notHandledMetaChangedForCurrentTrack = false
                        }


                        playerHandler?.removeMessages(DUCK)

                        playerHandler?.sendEmptyMessage(UNDUCK)

                        setLoudness(600)

                        playback?.start()


                        handleAndSendChangeInternal(PLAY_STATE_CHANGED)
                    }
                }

        }
    }


    private fun requestFocus(): Boolean {
        audioFocusRequestCompat = AudioFocusRequestCompat.Builder(

            AudioManagerCompat.AUDIOFOCUS_GAIN
        ).setOnAudioFocusChangeListener(
            audioFocusListener
        ).setAudioAttributes(
            AudioAttributesCompat
                .Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .build()
        ).build()

        return AudioManagerCompat.requestAudioFocus(
            audioManager!!,
            audioFocusRequestCompat!!
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }


    private fun setLoudness(millis: Long) {
        if(PreferenceUtil.mediaLoudNess != 50){
            try {
                loudnessEnhancer.release()
            } catch (e:Exception){}

            CoroutineScope(Dispatchers.IO).launch {

                delay(millis)
                loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                loudnessEnhancer.enabled = true
                val loudness = PreferenceUtil.mediaLoudNess
                if(loudness < 0 || loudness > 100){
                    PreferenceUtil.mediaLoudNess = 50
                }
                try {
                    loudnessEnhancer.setTargetGain(
                        (loudness - 50) * 50
                    )
                } catch (e: Exception){
                    println(e)
                }
            }
        }
    }


    fun seek(millis: Long): Long {
        synchronized(this) {
            return try {
                var newPosition: Long = 0
                if (playback != null) {
                    newPosition = playback!!.seek(millis)
                }
                throttledSeekHandler?.notifySeek()

                newPosition
            } catch (e: Exception) {
                -1
            }
        }
    }

    fun pause(){
        isPausedByTransientLossOfFocus = false
        if (
            playback != null &&
            playback!!.isPlaying
        ) {
            playback?.pause()

            handleAndSendChangeInternal(PLAY_STATE_CHANGED)
        }
    }

    fun forcePause() {
        isPausedByTransientLossOfFocus = false

        if (playback != null &&
            playback!!.isPlaying
        ) {
            playback?.pause()

            handleAndSendChangeInternal(PLAY_STATE_CHANGED)
        }
    }

    fun quit(){
        pause()
        stopForeground(true)
        notificationManager?.cancel(BasePlayingNotification.NOTIFICATION_ID)
        closeAudioEffectSession()

        if(!BuildUtil.isOreoPlus()){

            audioManager?.abandonAudioFocus(audioFocusListener)
        } else {
            if(
                audioManager != null &&
                audioFocusRequestCompat != null
            ){
                AudioManagerCompat.abandonAudioFocusRequest(
                    audioManager!!,
                    audioFocusRequestCompat!!
                )
            }
        }
        stopSelf()
    }

    private fun closeAudioEffectSession() {
        val audioEffectsIntent = Intent(
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
        )
        if (playback != null) {
            audioEffectsIntent.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                playback!!.audioSessionId
            )
        }
        audioEffectsIntent.putExtra(
            AudioEffect.EXTRA_PACKAGE_NAME,
            packageName
        )
        sendBroadcast(audioEffectsIntent)
    }


    fun playMediaAt(position: Int) {
        playerHandler?.removeMessages(PLAY_MEDIA)

        playerHandler?.obtainMessage(
            PLAY_MEDIA,
            position,
            0
        )?.sendToTarget()
    }

    fun playMediaAtImpl(position: Int) {
        if(openTrackAndPrepareNextAt(position)){
            play()
        } else {

            Toast.makeText(
                this,
                resources.getString(R.string.song),
                Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun openTrackAndPrepareNextAt(position: Int): Boolean {
        synchronized(this){
            this.position = position

            val prepared = openCurrent()
            if(prepared) {
                prepareNextImpl()
            }


            handleAndSendChangeInternal(META_CHANGED)

            notHandledMetaChangedForCurrentTrack = false
            return prepared
        }
    }


    private fun openCurrent(): Boolean {
        synchronized(this) {
            try {
                if(playback != null){
                    return playback!!.setDataSource(
                        getMediaUri(
                            Objects.requireNonNull(
                                currentMedia
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    fun prepareNextImpl() {
        synchronized(this){
            try {
                val nextPosition = getNextPosition()

                playback?.setNextDataSource(
                    getMediaUri(
                        getMediaAt(nextPosition)
                    )
                )

                this.nextPosition = nextPosition
            } catch (ignored: Exception){

            }
        }
    }


    fun handleAndSendChangeInternal(what: String) {

        handleChangeInternal(what)

        sendBroadcast(Intent(what))
    }

    private fun handleChangeInternal(what: String){
        when(what){


            PLAY_STATE_CHANGED -> {
                updateMediaSessionPlaybackState()

                val isPlaying = isPlaying
                if (!isPlaying && mediaProgressMillis > 0) {
                    savePositionInTrack()
                }

                playingNotification?.setPlaying(isPlaying) {
                    startForegroundOrNotify()
                }
                startForegroundOrNotify()
            }


            FAVORITE_STATE_CHANGED -> {

            }


            META_CHANGED -> {
                playingNotification?.updateMetadata(currentMedia) {
                    startForegroundOrNotify()
                }

                updateMediaSessionMetaData()
                updateMediaSessionPlaybackState()
                savePosition()
                savePositionInTrack()

                setLoudness(600)
            }


            QUEUE_CHANGED -> {
                updateMediaSessionMetaData()
                saveState()

                if (playingQueue.size > 0) {

                    prepareNext()
                } else {

                    stopForegroundAndNotification()
                }
            }
        }
    }


    fun saveState() {

        saveQueues()

        savePosition()

        savePositionInTrack()
    }

    private fun saveQueues() {
        queueSaveHandler?.removeMessages(SAVE_QUEUES)
        queueSaveHandler?.sendEmptyMessage(SAVE_QUEUES)
    }


    fun saveQueuesImpl() {

    }

    private fun setupMediaSession() {
        val mediaButtonReceiverComponentName = ComponentName(
            applicationContext,

            MediaButtonIntentReceiver::class.java
        )


        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON
        )
        mediaButtonIntent.component =
            mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent =

            PendingIntent.getBroadcast(
                applicationContext,
                0,
                mediaButtonIntent,

                if (BuildUtil.isMarshmallowPlus())
                    PendingIntent.FLAG_IMMUTABLE
                else 0
            )


        mediaSession = MediaSessionCompat(
            this,
            "MediaProvider",
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )

        val mediaSessionCallback = MediaSessionCallback(
            applicationContext,
            this
        )
        mediaSession?.setCallback(mediaSessionCallback)
        mediaSession?.isActive = true
        mediaSession?.setMediaButtonReceiver(
            mediaButtonReceiverPendingIntent
        )
    }


    fun updateMediaSessionMetaData() {
        Log.i(TAG, "onResourceReady: ")

        val media = currentMedia
        if (media.id == -1L) {

            mediaSession?.setMetadata(null)
            return
        }


        val metaData = MediaMetadataCompat.Builder()
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                media.artistName
            ).putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
                media.artistName
            ).putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                media.albumName
            ).putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                media.title
            ).putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                media.duration
            ).putLong(
                MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,

                (getPosition()).toLong()
            ).putLong(
                MediaMetadataCompat.METADATA_KEY_YEAR,
                media.year.toLong()
            ).putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                null
            )

        metaData.putLong(
            MediaMetadataCompat.METADATA_KEY_NUM_TRACKS,
            playingQueue.size.toLong()
        )


        mediaSession?.setMetadata(metaData.build())
    }

    fun updateMediaSessionPlaybackState() {
        stateBuilder = PlaybackStateCompat
            .Builder()
            .setActions(

                MEDIA_SESSION_ACTIONS
            ).setState(
                if(isPlaying)
                    PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED,
                mediaProgressMillis,
                1F
            )

        setCustomAction(stateBuilder)
        mediaSession?.setPlaybackState(stateBuilder.build())
    }

    private fun setCustomAction(
        stateBuilder: PlaybackStateCompat.Builder
    ) {

        var repeatIcon = R.drawable.ic_round_repeat

        if(repeatMode == REPEAT_MODE_ALL) {

            repeatIcon = R.drawable.ic_round_repeat_on
        }

        stateBuilder.addCustomAction(

            PlaybackStateCompat
                .CustomAction
                .Builder(

                    CYCLE_REPEAT,

                    getString(R.string.action_cycle_repeat),

                    repeatIcon
                ).build()
        )

        val shuffleIcon =
            if (getShuffleMode() == SHUFFLE_MODE_NONE)

                R.drawable.ic_round_shuffle

            else R.drawable.ic_round_shuffle_on

        stateBuilder.addCustomAction(
            PlaybackStateCompat
                .CustomAction.Builder(
                    TOGGLE_SHUFFLE,
                    getString(R.string.action_toggle_shuffle),
                    shuffleIcon
                ).build()
        )
    }

    private fun restoreState() {
        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(
            SAVED_SHUFFLE_MODE, 0
        )
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(
            SAVED_REPEAT_MODE, 0
        )
        sendBroadcast(Intent(SHUFFLE_MODE_CHANGED))
        sendBroadcast(Intent(REPEAT_MODE_CHANGED))
        playerHandler?.removeMessages(RESTORE_QUEUES)
        playerHandler?.sendEmptyMessage(RESTORE_QUEUES)
    }

    private fun getShuffleMode(): Int {
        return shuffleMode
    }

    private fun savePosition() {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit {
                putInt(
                    SAVED_POSITION,
                    getPosition()
                )
            }
    }

    fun savePositionInTrack() {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit {
                putLong(
                    SAVED_POSITION_IN_TRACK,
                    mediaProgressMillis
                )
            }
    }


    fun clearQueue() {
        playingQueue.clear()
        originalPlayingQueue.clear()

        setPosition(-1)
        handleAndSendChangeInternal(QUEUE_CHANGED)
    }


    private fun startForegroundOrNotify() {

        if (
            playingNotification != null &&
            currentMedia.id != -1L
        ) {
            val isPlaying = isPlaying

            if (
                isForeground != isPlaying &&
                !isPlaying
            ) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {

                    stopForeground(false)

                    isForeground = false
                }
            }


            if (!isForeground && isPlaying) {

                if (BuildUtil.isQPlus()) {

                    startForeground(
                        BasePlayingNotification.NOTIFICATION_ID,
                        playingNotification!!.build(),

                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {

                    startForeground(
                        BasePlayingNotification.NOTIFICATION_ID,
                        playingNotification!!.build()
                    )
                }

                isForeground = true

            } else {

                notificationManager?.notify(

                    BasePlayingNotification.NOTIFICATION_ID,

                    playingNotification!!.build()
                )
            }
        }
        return
    }


    fun stopNotification() {
        playerHandler?.removeMessages(STOP_NOTIFICATION)

        playerHandler?.obtainMessage(
            STOP_NOTIFICATION
        )?.sendToTarget()
    }


    fun stopForegroundAndNotification() {

        stopForeground(true)
        notificationManager?.cancel(
            BasePlayingNotification.NOTIFICATION_ID
        )
        isForeground = false
    }

    inner class MusicBinder: Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    companion object {
        const val MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
        const val RECENT_ROOT = "__RECENT__";
        const val MEDIA_ID_ROOT = "__ROOT__"

        const val PACKAGE_NAME = "com.example.elect.mediaplayer"
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val META_CHANGED = "$PACKAGE_NAME.metachanged"
        const val QUEUE_CHANGED = "$PACKAGE_NAME.queuechanged"
        const val PLAY_STATE_CHANGED = "$PACKAGE_NAME.playstatechanged"
        const val FAVORITE_STATE_CHANGED = "$PACKAGE_NAME.favoritestatechanged"
        const val REPEAT_MODE_CHANGED = "$PACKAGE_NAME.repeatmodechanged"
        const val SHUFFLE_MODE_CHANGED = "$PACKAGE_NAME.shufflemodechanged"
        const val CYCLE_REPEAT = "$PACKAGE_NAME.cyclerepeat"
        const val TOGGLE_SHUFFLE = "$PACKAGE_NAME.toggleshuffle"
        private const val MEDIA_SESSION_ACTIONS = (
                PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_SEEK_TO
                )

        const val SAVED_REPEAT_MODE = "REPEAT_MODE"
        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1

        const val SAVED_SHUFFLE_MODE = "SHUFFLE_MODE"
        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1

        const val SAVE_QUEUES = 0

        const val RELEASE_WAKELOCK = 0
        const val MEDIA_ENDED = 1
        const val MEDIA_WENT_TO_NEXT = 2
        const val PLAY_MEDIA = 3
        const val PREPARE_NEXT = 4
        const val SET_POSITION = 5
        const val FOCUS_CHANGE = 6
        const val DUCK = 7
        const val UNDUCK = 8
        const val RESTORE_QUEUES = 9
        const val NOT_PLAY_MEDIA = 10
        const val STOP_NOTIFICATION = 11


        const val ACTION_TOGGLE_PAUSE = "$MUSIC_PACKAGE_NAME.togglepause"
        const val ACTION_PLAY = "$MUSIC_PACKAGE_NAME.play"
        const val ACTION_PLAY_PLAYLIST = "$MUSIC_PACKAGE_NAME.play.playlist"
        const val ACTION_PAUSE = "$MUSIC_PACKAGE_NAME.pause"
        const val ACTION_STOP = "$MUSIC_PACKAGE_NAME.stop"
        const val ACTION_QUIT = "$MUSIC_PACKAGE_NAME.quitservice"
        const val ACTION_PREVIOUS = "$MUSIC_PACKAGE_NAME.previous"
        const val ACTION_SKIP = "$MUSIC_PACKAGE_NAME.skip"
        const val TOGGLE_FAVORITE = "$MUSIC_PACKAGE_NAME.togglefavorite"

        private fun getMediaUri(media: Media): String {

            return getMediaFileUri(media).toString()
        }
    }
}