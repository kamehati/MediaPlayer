package com.example.elect.mediaplayer.fragments.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.fragments.player.MusicSkipTouchListener
import com.example.elect.mediaplayer.helper.MusicProgressViewUpdateHelper
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.MusicUtil

abstract class BaseActionControlsFragment(
    @LayoutRes layout: Int
): BaseMusicServiceFragment(layout),
    MusicProgressViewUpdateHelper.Callback{

    protected abstract fun show()

    protected abstract fun hide()



    var isSeeking = false

        private set

    open val progressSlider: SeekBar? = null

    abstract val shuffleButton: ImageButton

    abstract val repeatButton: ImageButton

    open val nextButton: ImageButton? = null

    open val previousButton: ImageButton? = null

    open val songTotalTime: TextView? = null

    open val songCurrentProgress: TextView? = null

    private var progressAnimator: ObjectAnimator? = null

    open val volumeDown: ImageView? = null

    open val progressVolume: SeekBar? = null

    open val volumeUp: ImageView? = null


    override fun onUpdateProgressViews(
        progress: Long,
        total: Long
    ) {

        progressSlider?.max = total.toInt()

        if (isSeeking) {

            progressSlider?.progress = progress.toInt()
        } else {
            progressAnimator =
                ObjectAnimator.ofInt(
                    progressSlider,
                    "progress",
                    progress.toInt()
                ).apply {

                    duration = SLIDER_ANIMATION_TIME

                    interpolator = LinearInterpolator()
                    start()
                }
        }


        songTotalTime?.text =
            MusicUtil.getReadableDurationString(total)

        songCurrentProgress?.text =
            MusicUtil.getReadableDurationString(progress)
    }


    private fun setUpProgressSlider() {

        progressSlider?.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {

                        onUpdateProgressViews(
                            progress.toLong(),

                            PlayerRemote.mediaDurationMillis
                        )
                    }
                }


                override fun onStartTrackingTouch(
                    seekBar: SeekBar
                ) {

                    isSeeking = true

                    progressViewUpdateHelper.stop()


                    progressAnimator?.cancel()
                }


                override fun onStopTrackingTouch(
                    seekBar: SeekBar
                ) {

                    isSeeking = false

                    PlayerRemote.seekTo(seekBar.progress.toLong())


                    progressViewUpdateHelper.start()
                }
            }
        )
    }

    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }


    fun View.showBounceAnimation() {

        clearAnimation()




        scaleX = 0.9f

        scaleY = 0.9f

        isVisible = true


        pivotX = (width / 2).toFloat()

        pivotY = (height / 2).toFloat()


        animate()
            .setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .scaleX(1.1f)
            .scaleY(1.1f)
            .withEndAction {
                animate()
                    .setDuration(200)
                    .setInterpolator(AccelerateInterpolator())

                    .scaleX(1f)
                    .scaleY(1f)

                    .alpha(1f)
                    .start()
            }
            .start()
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onStart() {
        super.onStart()

        setUpProgressSlider()

        setUpPrevNext()

        setUpShuffleButton()

        setUpRepeatButton()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPrevNext() {
        nextButton?.apply {
            setOnTouchListener(
                MusicSkipTouchListener(
                    requireActivity(),
                    true
                )
            )
        }

        previousButton?.apply {
            setOnTouchListener(
                MusicSkipTouchListener(
                    requireActivity(),
                    false
                )
            )
        }
    }


    private fun setUpShuffleButton() {
        shuffleButton.setOnClickListener {
            PlayerRemote.toggleShuffleMode()
        }
    }


    private fun setUpRepeatButton() {
        repeatButton.setOnClickListener {
            PlayerRemote.cycleRepeatMode()
        }
    }

    fun updatePrevNextColor() {

        nextButton?.iconColor(R.color.md_white_1000)
        previousButton?.iconColor(R.color.md_white_1000)
    }

    fun updateShuffleState() {
        when (PlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_NONE ->
                shuffleButton.setImageResource(
                    R.drawable.ic_round_shuffle
                )

            MusicService.SHUFFLE_MODE_SHUFFLE ->
                shuffleButton.setImageResource(
                    R.drawable.ic_round_shuffle_on
                )
        }
    }

    fun updateRepeatState() {
        when (PlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {

                repeatButton.setImageResource(
                    R.drawable.ic_round_repeat
                )
            }

            MusicService.REPEAT_MODE_ALL -> {
                repeatButton.setImageResource(
                    R.drawable.ic_round_repeat_on
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateShuffleState()
        updateRepeatState()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()

        progressViewUpdateHelper.stop()
    }

    companion object {
        const val SLIDER_ANIMATION_TIME: Long = 400
    }
}