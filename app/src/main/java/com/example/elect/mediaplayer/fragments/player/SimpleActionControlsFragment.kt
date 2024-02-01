package com.example.elect.mediaplayer.fragments.player

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.FragmentSimpleActionControlsBinding
import com.example.elect.mediaplayer.extensions.backgroundColor
import com.example.elect.mediaplayer.extensions.seekbarColor
import com.example.elect.mediaplayer.extensions.textColor
import com.example.elect.mediaplayer.fragments.base.BaseActionControlsFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.volume.AudioVolumeObserver

class SimpleActionControlsFragment:
    BaseActionControlsFragment(
        R.layout.fragment_simple_action_controls
    ){

    override val progressSlider: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)

            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    private var _binding : FragmentSimpleActionControlsBinding? = null
    private val binding get() =  _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSimpleActionControlsBinding.bind(view)

        setUpProgressSlider()


        setUpPlayPauseFab()

        updatePrevNextColor()
    }

    private fun setUpProgressSlider() {

        progressSlider.seekbarColor(
            R.color.md_blue_500,
            R.color.md_grey_400
        )
        songCurrentProgress.textColor(R.color.md_grey_400)
        songTotalTime.textColor(R.color.md_grey_400)
    }


    private fun setUpPlayPauseFab() {
        binding.playPauseButton.apply {
            setOnClickListener {
                if (PlayerRemote.isPlaying) {
                    PlayerRemote.pauseMedia()
                }
                else {
                    PlayerRemote.resumeMedia()
                }

                it.showBounceAnimation()
            }

            backgroundColor(
                R.color.md_blue_500,
                R.color.md_white_1000
            )
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()

        updateMedia()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()

        updateShuffleState()

        updateRepeatState()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()

        updateRepeatState()

        updateShuffleState()

        updateMedia()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
        updateShuffleState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
        updateRepeatState()
    }


    private fun updatePlayPauseDrawableState() {

        if (PlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(
                R.drawable.ic_round_pause
            )
        }

        else {
            binding.playPauseButton.setImageResource(
                R.drawable.ic_round_play_arrow
            )
        }
    }



    private fun updateMedia() {

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

        builder.append(title)
        binding.title.isSelected = true
        binding.title.text = builder


        binding.text.textColor(R.color.md_grey_400)
        binding.text.text = media.artistName
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}