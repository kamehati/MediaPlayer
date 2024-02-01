package com.example.elect.mediaplayer.fragments.other

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.FragmentMiniPlayerBinding
import com.example.elect.mediaplayer.extensions.backGroundColor
import com.example.elect.mediaplayer.fragments.base.BaseActivityFragment
import com.example.elect.mediaplayer.helper.MusicProgressViewUpdateHelper
import com.example.elect.mediaplayer.helper.PlayPauseButtonOnClickHandler
import com.example.elect.mediaplayer.helper.PlayerRemote
import kotlin.math.abs

class MiniPlayerFragment:
    BaseActivityFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback,
    View.OnClickListener {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressViewUpdateHelper
    : MusicProgressViewUpdateHelper

    override fun onClick(view: View) {
        when(view.id){
            R.id.actionNext -> {
                PlayerRemote.playNextMedia()
            }

            R.id.actionPrevious ->{
                PlayerRemote.playPreviousMedia()
            }

            R.id.actionClose -> {

                PlayerRemote.quit()
                PlayerRemote.clearQueue()
                mainActivity.selectHideBottom()
            }
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        progressViewUpdateHelper =
            MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMiniPlayerBinding.bind(view)


        view.setOnTouchListener(

            FlingPlayBackController(requireContext())
        )


        setUpMiniPlayer()

        setUpPlayerButton()
    }


    private fun setUpMiniPlayer() {

        binding.miniPlayerFrameLayout.backGroundColor(R.color.md_blue_grey_900)


        setUpPlayPauseButton()

        binding.progressBar.apply{
            val color = ContextCompat.getColor(
                requireContext(),
                R.color.md_blue_500
            )
            val color2 = ContextCompat.getColor(
                requireContext(),
                R.color.md_blue_grey_600
            )
            setIndicatorColor(color)

            trackColor = color2
        }
    }


    private fun setUpPlayPauseButton() {
        binding.miniPlayerPlayPauseButton
            .setOnClickListener(

                PlayPauseButtonOnClickHandler()
            )
    }


    private fun setUpPlayerButton() {
        binding.actionNext.apply {
            isVisible = true
            setOnClickListener(this@MiniPlayerFragment)
        }
        binding.actionPrevious.apply {
            isVisible = true
            setOnClickListener(this@MiniPlayerFragment)
        }
        binding.actionClose.apply {
            isVisible = true
            setOnClickListener(this@MiniPlayerFragment)
        }
    }

    override fun onServiceConnected() {
        updateMediaTitle()

        updatePlayPauseDrawableState()
    }

    private fun updateMediaTitle() {

        val media = PlayerRemote.currentMedia


        val builder = SpannableStringBuilder()

        val title = SpannableString(media.title)

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

        val text = SpannableString(media.artistName)
        text.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_white_1000
                )
            ),
            0,
            text.length,
            0
        )

        if(media.isSongOrVideo == 1){
            builder.append(title)
                .append(" • ")
                .append(text)
        } else if(media.isSongOrVideo == 2){
            builder.append(title)
        }

        binding.miniPlayerTitle.isSelected = true
        binding.miniPlayerTitle.text = builder
    }

    protected fun updatePlayPauseDrawableState() {
        if (PlayerRemote.isPlaying) {
            binding.miniPlayerPlayPauseButton
                .setImageResource(R.drawable.ic_round_pause)
        } else {
            binding.miniPlayerPlayPauseButton
                .setImageResource(R.drawable.ic_round_play_arrow)
        }
    }


    override fun onPlayingMetaChanged() {
        updateMediaTitle()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onUpdateProgressViews(
        progress: Long,
        total: Long
    ) {
        binding.progressBar.max = total.toInt()
        val animator = ObjectAnimator.ofInt(
            binding.progressBar,
            "progress",
            progress.toInt()
        )

        animator.duration = 1000

        animator.interpolator = DecelerateInterpolator()

        animator.start()
    }

    override fun onResume() {
        super.onResume()


        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()


        progressViewUpdateHelper.stop()
    }

    class FlingPlayBackController(context: Context)

        : View.OnTouchListener {


        private var flingPlayBackController =

            GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {

                    override fun onFling(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {

                        if (abs(velocityX) > abs(velocityY)) {

                            if (velocityX < 0) {
                                PlayerRemote.playPreviousMedia()
                                return true

                            } else if (velocityX > 0) {
                                PlayerRemote.playNextMedia()
                                return true
                            }
                        }
                        return false
                    }

                }
            )


        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(
            v: View,
            event: MotionEvent
        ): Boolean {

            return flingPlayBackController
                .onTouchEvent(event)
        }
    }
}