package com.example.elect.mediaplayer.fragments.player

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Rational
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.databinding.FeaturesMenuBinding
import com.example.elect.mediaplayer.databinding.FragmentVideoPlayerBinding
import com.example.elect.mediaplayer.dialogs.ExoLoudnessDialog
import com.example.elect.mediaplayer.dialogs.QueueDialogFragment
import com.example.elect.mediaplayer.extensions.seekbarColor
import com.example.elect.mediaplayer.extensions.statusBarColor
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseMusicServiceFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import java.util.ArrayList
import kotlin.math.abs

class VideoPlayerFragment :
    BaseMusicServiceFragment(R.layout.fragment_video_player),
    GestureDetector.OnGestureListener{

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var exoPlayerLoudnessEnhancer: LoudnessEnhancer


    val libraryViewModel: LibraryViewModel by sharedViewModel()

    private lateinit var videoTitle: MaterialTextView
    private lateinit var featuresBtn: ImageButton
    private lateinit var playPauseBtn: ImageButton
    private lateinit var prevBtn: ImageButton
    private lateinit var nextBtn: ImageButton
    private lateinit var fullScreenBtn: ImageButton
    private lateinit var repeatBtn: ImageButton
    private lateinit var shuffleBtn: ImageButton
    private lateinit var orientationBtn: ImageButton
    private lateinit var seekBar: DefaultTimeBar
    private lateinit var favoriteBtn: ImageButton
    private var backBtn: ImageButton? = null

    private var player: ExoPlayer? = null
    private var playingVideoQueue = mutableListOf<Media>()
    private var videoPosition: Int = -1
    private lateinit var gestureDetectorCompat: GestureDetectorCompat

    private var isLocked: Boolean = false

    private var isFullscreen: Boolean = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentVideoPlayerBinding.bind(view)

        videoTitle = view.findViewById(R.id.videoTitle)
        featuresBtn = view.findViewById(R.id.moreFeaturesBtn)
        playPauseBtn = view.findViewById(R.id.playPauseBtn)
        prevBtn = view.findViewById(R.id.prevBtn)
        nextBtn = view.findViewById(R.id.nextBtn)
        fullScreenBtn = view.findViewById(R.id.fullScreenBtn)
        repeatBtn = view.findViewById(R.id.repeatBtn)
        shuffleBtn = view.findViewById(R.id.shuffleBtn)
        orientationBtn = view.findViewById(R.id.orientationBtn)
        seekBar = view.findViewById(com.google.android.exoplayer2.ui.R.id.exo_progress)
        favoriteBtn = view.findViewById(R.id.favoriteBtn)
        backBtn = view.findViewById(R.id.backBtn)

        videoTitle.text = "サンプル"

        setUpTopBtn()
        setUpPlayerBtn()
        setUpBottomBtn()

        updateRepeatState()
        updateShuffleState()

        gestureDetectorCompat =
            GestureDetectorCompat(requireContext(), this)

        binding.playerView.controllerShowTimeoutMs = 2500
        binding.playerView.useController = true
        binding.playerView.showController()

        createPlayer(
            PlayerRemote.position,
            PlayerRemote.playingQueue
        )

        setStatusBar()
    }

    private fun setStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(
            requireActivity().window,
            false
        )

        WindowInsetsControllerCompat(
            requireActivity().window,
            binding.root
        ).let { controller ->

            controller.hide(
                WindowInsetsCompat.Type.systemBars()
            )
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat
                    .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun updateShuffleState() {
        when (PlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_NONE ->
                shuffleBtn.setImageResource(
                    R.drawable.ic_round_shuffle
                )

            MusicService.SHUFFLE_MODE_SHUFFLE ->
                shuffleBtn.setImageResource(
                    R.drawable.ic_round_shuffle_on
                )
        }
    }

    fun updateRepeatState() {
        when (PlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {

                repeatBtn.setImageResource(
                    R.drawable.ic_round_repeat
                )
            }

            MusicService.REPEAT_MODE_ALL -> {
                repeatBtn.setImageResource(
                    R.drawable.ic_round_repeat_on
                )
            }
        }
    }

    private fun setUpTopBtn(){
        binding.lockButton.setOnClickListener {

            if(!isLocked){
                isLocked = true

                binding.playerView.hideController()
                binding.playerView.useController = false
                binding.lockButton.setImageResource(
                    R.drawable.ic_round_lock
                )
            }
            else{
                isLocked = false
                binding.playerView.useController = true

                binding.playerView.showController()
                binding.lockButton.setImageResource(
                    R.drawable.ic_round_lock_open
                )
            }
        }

        featuresBtn.setOnClickListener {
            setUpFeaturesMenu()
        }

        favoriteBtn.setOnClickListener {
            toggleFavorite(PlayerRemote.currentMedia)
        }
    }

    private fun setUpFeaturesMenu() {
        val customDialog = LayoutInflater
            .from(requireContext())
            .inflate(
                R.layout.features_menu,
                binding.root,
                false
            )
        val bindingFM = FeaturesMenuBinding.bind(
            customDialog
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customDialog)
            .setOnCancelListener {
                it.dismiss()
            }

            .setBackground(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_blue_grey_transparent_900
                    )
                )
            ).create()
        dialog.show()

        setUpFMBtn(bindingFM, dialog)
    }

    private fun setUpFMBtn(
        binding: FeaturesMenuBinding,
        fmDialog: AlertDialog
    ) {
        binding.actionLoudness.setOnClickListener {
            val activity = requireActivity() as PlayerActivity
            ExoLoudnessDialog.newInstance(
                activity,
                player!!.audioSessionId
            ).show(
                activity.supportFragmentManager,
                "EXO_LOUDNESS_DIALOG"
            )
            fmDialog.dismiss()
        }
        binding.actionQueue.setOnClickListener {
            val dialog = QueueDialogFragment.newInstance()
            requireActivity().supportFragmentManager.let {
                dialog.show(it, dialog.tag)
            }
            fmDialog.dismiss()
        }

        binding.actionPIPmode.setOnClickListener {
            fmDialog.dismiss()


            val media = PlayerRemote.currentMedia

            mediaSession = MediaSessionCompat(requireContext(), "ExoPlayer").apply {
                isActive = true
            }
            mediaSessionConnector = MediaSessionConnector(mediaSession)
            if(player != null){
                mediaSessionConnector.setPlayer(player!!)
            }

            mediaSessionConnector.setMediaMetadataProvider {
                val mediaMetadataCompat = MediaMetadataCompat.Builder().apply {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, media.duration)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.artistName)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, media.artistName)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, media.albumName)
                    putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, (PlayerRemote.position).toLong())

                    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.title)
                    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, media.title)
                    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, media.artistName)
                    putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, PlayerRemote.playingQueue.size.toLong())
                }.build()
                mediaMetadataCompat
            }

            if(hasPIPPermission(requireActivity())){
                if(BuildUtil.isQPlus()){
                    val builder = PictureInPictureParams.Builder()

                    this.binding.playerView.apply {
                        hideController()
                        useController = false
                    }
                    builder.setAspectRatio(Rational(16, 9))
                    requireActivity().enterPictureInPictureMode(
                        builder.build()
                    )


                    playVideo()

                    pipStatus = 1

                    PlayerRemote.clearQueue()
                }

                else {

                    val intent = Intent(
                        "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                        Uri.parse("package:${requireActivity().packageName}")
                    )

                    startActivity(intent)
                }
            }
            else{
                Toast.makeText(
                    requireContext(),
                    "Feature Not Supported!!",
                    Toast.LENGTH_SHORT
                ).show()

                fmDialog.dismiss()
            }

        }
    }

    private fun hasPIPPermission(
        context: Context
    ): Boolean {

        val appOps = context
            .getSystemService(
                Context.APP_OPS_SERVICE
            ) as AppOpsManager

        val status = if (
            BuildUtil.isOreoPlus()
        ) {
            if(BuildUtil.isQPlus()){
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    context.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } else {

                appOps.checkOpNoThrow(

                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,

                    android.os.Process.myUid(),
                    context.packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
        } else {
            false
        }

        return status
    }


    var pipStatus: Int = 0

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {
        if(isInPictureInPictureMode){

        } else {

            mediaSessionConnector.setPlayer(null)

            requireActivity().finish()
            mediaSession.isActive = false
        }

        if(pipStatus != 1){

            requireActivity().finish()
            mediaSession.isActive = false
        }

        if(!isInPictureInPictureMode)

            pauseVideo()
    }

    override fun onConfigurationChanged(
        newConfig: Configuration
    ) {
        super.onConfigurationChanged(newConfig)
    }

    private fun toggleFavorite(media: Media) {

        lifecycleScope.launch(Dispatchers.IO) {


            val isFavorite = libraryViewModel.isMediaFavoriteEntity(media.id)

            libraryViewModel.apply {

                insertOrUpdateMediaFavoriteEntity(media, !isFavorite)

                forceReload(ReloadType.Favorites)
                forceReload(ReloadType.FavoriteMedias)
                forceReload(ReloadType.Songs)
                forceReload(ReloadType.Playlists)
            }

            withContext(Dispatchers.Main){
                val icon = if(!isFavorite){
                    GlideExtensions.DEFAULT_FAVORITE_TRUE
                }else {
                    GlideExtensions.DEFAULT_FAVORITE_FALSE
                }

                favoriteBtn.setImageResource(icon)
            }
        }
    }

    private fun setUpIsFavorite() {
        val media = PlayerRemote.currentMedia
        CoroutineScope(Dispatchers.IO).launch {

            val isFavorite = libraryViewModel.isMediaFavoriteEntity(media.id)

            withContext(Dispatchers.Main){
                val icon = if(isFavorite){
                    GlideExtensions.DEFAULT_FAVORITE_TRUE
                }else {
                    GlideExtensions.DEFAULT_FAVORITE_FALSE
                }

                favoriteBtn.setImageResource(icon)
            }
        }
    }

    private fun setUpPlayerBtn(){
        playPauseBtn.setOnClickListener {
            if(player?.isPlaying == true) pauseVideo()
            else playVideo()
        }
        prevBtn.setOnClickListener {
            nextPrevVideo(isNext = false)


            playVideo()

        }
        nextBtn.setOnClickListener {
            nextPrevVideo(isNext = true)

            playVideo()
        }
    }

    private fun pauseVideo(){
        playPauseBtn.setImageResource(
            R.drawable.ic_round_play_arrow
        )
        player?.pause()
    }

    fun playVideo(){
        playPauseBtn.setImageResource(
            R.drawable.ic_round_pause
        )
        player?.play()
    }

    private fun setUpBottomBtn(){
        backBtn?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        orientationBtn.setOnClickListener {
            requireActivity().requestedOrientation = if(
                resources.configuration.orientation ==
                Configuration.ORIENTATION_PORTRAIT
            )
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        repeatBtn.setOnClickListener {
            PlayerRemote.cycleRepeatMode()
        }
        shuffleBtn.setOnClickListener {
            PlayerRemote.toggleShuffleMode()
        }
        fullScreenBtn.setOnClickListener {
            if(isFullscreen){
                isFullscreen = false
                playInFullscreen(enable = false)
            }
            else{
                isFullscreen = true
                playInFullscreen(enable = true)
            }
        }
    }

    private fun playInFullscreen(enable: Boolean){

        if(enable){
            binding.playerView.resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_FILL
            if(player != null){
                player!!.videoScalingMode =
                    C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }

            fullScreenBtn.setImageResource(
                R.drawable.ic_round_fullscreen)
        }
        else{
            binding.playerView.resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            if(player != null){
                player!!.videoScalingMode =
                    C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
            fullScreenBtn.setImageResource(
                R.drawable.ic_round_fullscreen_exit)
        }
    }

    override fun onResume() {
        super.onResume()

        if (audioManager == null) audioManager =
            requireContext().getSystemService()

        if (brightness != 0)
            setScreenBrightness(brightness)

        playVideo()
        player?.seekTo(PlayerRemote.mediaProgressMillis)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                remove()
                requireActivity().finish()
            }

        PlayerRemote.stopForegroundAndNotification()
        PreferenceUtil.nowPlayingFragment = 1

        setLoudness()

        if(pipStatus == 1){
            this.binding.playerView.apply {
                showController()
                useController = true
            }

            pipStatus = 0
        }
    }

    override fun onPause() {
        super.onPause()
        if(pipStatus != 1){
            player?.pause()
        }
        PreferenceUtil.nowPlayingFragment = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if(pipStatus != 1){
            player?.pause()
            player?.release()
        }
    }

    fun playerClose(){
        pauseVideo()
        player?.release()
    }

    fun createPlayer(
        position: Int,
        dataSet: List<Media>
    ) {
        try {
            player?.release()
        } catch (e: Exception){ }

        videoPosition = position
        playingVideoQueue = ArrayList(dataSet)
        val video = playingVideoQueue[videoPosition]

        val builder = SpannableStringBuilder()
        val title = SpannableString(video.title)
        title.setSpan(

            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_white_1000
                )
            ),

            0,

            title.length,
            0
        )
        builder.append(title)
        videoTitle.isSelected = true
        videoTitle.text = builder

        player = ExoPlayer
            .Builder(requireContext())
            .build()

        doubleTapEnable()

        val file = File(video.data)
        val artUri = Uri.fromFile(file)

        val mediaItem = MediaItem.fromUri(
            artUri
        )
        player!!.setMediaItem(mediaItem)


        val attr = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        player!!.setHandleAudioBecomingNoisy(true)

        player!!.prepare()

        player!!.seekTo(PlayerRemote.mediaProgressMillis)

        player!!.addListener(
            object : Player.Listener{
                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    super.onPlaybackStateChanged(playbackState)

                    if(
                        playbackState == Player.STATE_ENDED
                    ){
                        nextPrevVideo(isNext = true)

                        if(player?.isPlaying == true){
                            playPauseBtn.setImageResource(
                                R.drawable.ic_round_pause
                            )
                        } else {
                            playPauseBtn.setImageResource(
                                R.drawable.ic_round_play_arrow
                            )

                                playVideo()
                        }

                    } else if(playbackState == Player.STATE_BUFFERING) {
                        setUpIsFavorite()
                        playPauseBtn.setImageResource(
                            R.drawable.ic_round_empty
                        )

                        setLoudness()
                    } else if(playbackState != Player.STATE_BUFFERING) {
                        setUpIsFavorite()
                        if(player != null){
                            if(!player!!.isPlaying){
                                pauseVideo()
                            } else if(player!!.isPlaying){
                                playVideo()
                            }
                        }

                        setLoudness()
                    }
                }
            }
        )

        seekBarFeature()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun doubleTapEnable() {
        binding.playerView.player = player

        binding.ytOverlay.performListener(
            object : YouTubeOverlay.PerformListener{
                private var isPlaying = true

                override fun onAnimationStart() {
                    isPlaying = player?.isPlaying ?: false
                    binding.ytOverlay.visibility = View.VISIBLE
                }

                override fun onAnimationEnd() {
                    binding.ytOverlay.visibility = View.GONE

                    if(player != null){
                        if(isPlaying){
                            playVideo()
                        } else if(!isPlaying){
                            pauseVideo()
                        }
                    }
                }
            }
        )

        player?.let { binding.ytOverlay.player(it) }

        gestureDetectorCompat =
            GestureDetectorCompat(requireContext(), this)

        binding.playerView.setOnTouchListener { _, motionEvent ->
            binding.playerView.isDoubleTapEnabled = false

            if(!isLocked){
                binding.playerView.isDoubleTapEnabled = true
                gestureDetectorCompat.onTouchEvent(motionEvent)

                if(motionEvent.action == MotionEvent.ACTION_UP) {
                    binding.brightnessIcon.visibility = View.GONE
                    binding.volumeIcon.visibility = View.GONE

                    setStatusBar()
                }
            }
            return@setOnTouchListener false
        }

        binding.playerView
            .setControllerVisibilityListener {
                when{
                    isLocked -> {
                        binding.lockButton.visibility = View.VISIBLE
                    }

                    binding.playerView.isControllerVisible -> {
                        binding.lockButton.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.lockButton.visibility = View.INVISIBLE
                    }
                }
            }
    }

    private fun nextPrevVideo(isNext: Boolean = true) {
        if(isNext){
            videoPosition = when(PlayerRemote.repeatMode){

                MusicService.REPEAT_MODE_NONE -> {
                    if(
                        PlayerRemote.position + 1 >
                        PlayerRemote.playingQueue.size - 1
                    ){
                        PlayerRemote.position
                    } else {
                        PlayerRemote.position + 1
                    }
                }

                MusicService.REPEAT_MODE_ALL -> {
                    if(
                        PlayerRemote.position + 1 >
                        PlayerRemote.playingQueue.size - 1
                    ){
                        0
                    } else {
                        PlayerRemote.position + 1
                    }
                }

                else -> {
                    -1
                }
            }

            val isNextSongOrVideo = PlayerRemote.playingQueue[videoPosition].isSongOrVideo

            PlayerRemote.playNextMedia()

            if(activity is PlayerActivity){
                (activity as PlayerActivity).createPlayerFragment(
                    isNextSongOrVideo
                )
            }
        } else {
            if(PlayerRemote.mediaProgressMillis > 2000){
                PlayerRemote.playPreviousMedia()
            } else {
                videoPosition = when(PlayerRemote.repeatMode){

                    MusicService.REPEAT_MODE_NONE -> {
                        if(
                            PlayerRemote.position - 1 < 0
                        ){
                            0
                        } else {
                            PlayerRemote.position - 1
                        }
                    }

                    MusicService.REPEAT_MODE_ALL -> {
                        if(
                            PlayerRemote.position - 1 < 0
                        ){
                            PlayerRemote.playingQueue.size - 1
                        } else {
                            PlayerRemote.position - 1
                        }
                    }

                    else -> {
                        -1
                    }
                }
                if(videoPosition < 0){
                    videoPosition = 0
                }
                val isPreviousSongOrVideo = PlayerRemote.playingQueue[videoPosition].isSongOrVideo

                PlayerRemote.playPreviousMedia()

                if(activity is PlayerActivity){
                    (activity as PlayerActivity).createPlayerFragment(
                        isPreviousSongOrVideo
                    )
                }
            }
        }

        if(PlayerRemote.playingQueue[videoPosition].isSongOrVideo == 2){
            createPlayer(videoPosition, PlayerRemote.playingQueue)
        }
    }

    private fun seekBarFeature(){
        seekBar.apply {
            seekbarColor(
                R.color.md_blue_500,
                R.color.md_grey_400
            )

            addListener(
                object : TimeBar.OnScrubListener{
                    override fun onScrubStart(
                        timeBar: TimeBar,
                        position: Long
                    ) {

                    }

                    override fun onScrubMove(
                        timeBar: TimeBar,
                        position: Long
                    ) {
                        player?.seekTo(position)
                    }

                    override fun onScrubStop(
                        timeBar: TimeBar,
                        position: Long,
                        canceled: Boolean
                    ) {

                    }
                }
            )
        }
    }

    fun putVideoProgress() {
        if(player?.currentPosition != null){
            PreferenceUtil.videoProgressMillis = player?.currentPosition!!
        } else {
            PreferenceUtil.videoProgressMillis = 0L
        }
    }

    private var minSwipeY: Float = 0f
    private var brightness: Int = 0
    private var audioManager: AudioManager? = null
    private var volume: Int = 0


    override fun onDown(p0: MotionEvent): Boolean {
        minSwipeY = 0f
        return false
    }

    override fun onShowPress(p0: MotionEvent) = Unit


    override fun onSingleTapUp(
        p0: MotionEvent
    ): Boolean = false

    override fun onScroll(

        event: MotionEvent?,
        event1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        minSwipeY += distanceY

        val sWidth = Resources.getSystem()
            .displayMetrics.widthPixels

        val sHeight = Resources.getSystem()
            .displayMetrics.heightPixels

        val border = 100 * Resources.getSystem()
            .displayMetrics.density.toInt()

        if(
            event!!.x < border ||
            event.y < border ||
            event.x > sWidth - border ||
            event.y > sHeight - border
        ) {
            return false
        } else {
            requireView().parent.requestDisallowInterceptTouchEvent(true)

            if(
                abs(distanceX) < abs(distanceY) &&
                abs(minSwipeY) > 50
            ){
                if(event.x < sWidth/2){
                    binding.brightnessIcon.visibility = View.VISIBLE
                    binding.volumeIcon.visibility = View.GONE
                    val increase = distanceY > 0
                    val newValue = if(increase) brightness + 5 else brightness - 5
                    if(newValue in 0..100) brightness = newValue
                    val s = requireContext().getString(
                        R.string.brightness_property,
                        brightness.toString()
                    )
                    binding.brightnessIcon.text = s
                    setScreenBrightness(brightness)
                }
                else{
                    binding.brightnessIcon.visibility = View.GONE
                    binding.volumeIcon.visibility = View.VISIBLE
                    val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val increase = distanceY > 0
                    val newValue = if(increase) volume + 1 else volume - 1
                    if(newValue in 0..maxVolume) volume = newValue
                    binding.volumeIcon.text = volume.toString()
                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                }
                minSwipeY = 0f
            }

            return true
        }
    }

    private fun setScreenBrightness(value: Int){
        PreferenceUtil.brightNess = value
        val d = 1.0f/100
        val lp = requireActivity().window.attributes
        lp.screenBrightness = d * value
        requireActivity().window.attributes = lp
    }

    override fun onLongPress(p0: MotionEvent) = Unit

    override fun onFling(
        p0: MotionEvent?,
        p1: MotionEvent,
        p2: Float,
        p3: Float
    ): Boolean = false

    override fun onPlayingMetaChanged() {
        if(PreferenceUtil.nowPlayingFragment == 1){
        } else {
            setLoudness()
        }
    }

    override fun onPlayStateChanged() {
        if(PreferenceUtil.nowPlayingFragment == 1){
            if(player?.isPlaying == true){
                if(!PlayerRemote.isPlaying){
                    PlayerRemote.resumeMedia()
                }
            } else {
                if(PlayerRemote.isPlaying){
                    PlayerRemote.pauseMedia()
                }
                PlayerRemote.stopForegroundAndNotification()
            }
        }
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
        PlayerRemote.stopForegroundAndNotification()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
        PlayerRemote.stopForegroundAndNotification()
    }

    private fun setLoudness(){
        setUpLoudness()
        val mediaLoudness = PreferenceUtil.mediaLoudNess
        val exoLoudness = PreferenceUtil.exoLoudNess
        if(mediaLoudness < 0 || mediaLoudness > 100){
            PreferenceUtil.mediaLoudNess = 50
        }
        if(exoLoudness < 0 || exoLoudness > 100){
            PreferenceUtil.mediaLoudNess = 50
        }

        if(PreferenceUtil.nowPlayingFragment == 1){
            exoPlayerLoudnessEnhancer.enabled = true

            exoPlayerLoudnessEnhancer.setTargetGain(
                (exoLoudness - 50) * 50
            )
        }
    }

    private fun setUpLoudness(){
        if(player != null){
            exoPlayerLoudnessEnhancer = LoudnessEnhancer(player!!.audioSessionId)
        }
        exoPlayerLoudnessEnhancer.enabled = false
    }
}