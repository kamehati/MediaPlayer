package com.example.elect.mediaplayer.fragments.player

import android.media.AudioManager
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.FragmentPlayerActionControlsBinding
import com.example.elect.mediaplayer.extensions.backgroundColor
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.extensions.seekbarColor
import com.example.elect.mediaplayer.extensions.textColor
import com.example.elect.mediaplayer.fragments.base.BaseActionControlsFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.volume.AudioVolumeObserver
import com.example.elect.mediaplayer.volume.OnAudioVolumeChangedListener

class PlayerActionControlsFragment:
    BaseActionControlsFragment(
        R.layout.fragment_player_action_controls
    ),
    OnAudioVolumeChangedListener,
    SeekBar.OnSeekBarChangeListener{

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

    override val volumeDown: AppCompatImageView
        get() = binding.volumeDown

    override val progressVolume: SeekBar
        get() = binding.volumeSeekBar

    override val volumeUp: AppCompatImageView
        get() = binding.volumeUp

    fun buttonShow() {
        binding.playPauseButton.visibility = View.INVISIBLE
    }

    public override fun show() {
        binding.playPauseButton.visibility = View.VISIBLE

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

    private var _binding : FragmentPlayerActionControlsBinding? = null
    private val binding get() = _binding!!

    private var audioVolumeObserver: AudioVolumeObserver? = null

    private val audioManager: AudioManager
        get() = requireContext().getSystemService()!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlayerActionControlsBinding.bind(view)

        setUpVolumeUI()

        setUpProgressSlider()

        setUpPlayPauseFab()

        updatePrevNextColor()
    }

    private fun setUpVolumeUI() {
        volumeDown.apply {
            iconColor(R.color.md_grey_400)

            setOnClickListener{
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    0
                )
            }
        }
        progressVolume.seekbarColor(
            R.color.md_blue_500,
            R.color.md_grey_400
        )
        volumeUp.apply {
            iconColor(R.color.md_grey_400)
            setOnClickListener{
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,

                    AudioManager.ADJUST_RAISE,
                    0
                )
            }
        }
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
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()

        updateRepeatState()

        updateShuffleState()

        updateMedia()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
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

        binding.text.text = media.artistName

        binding.text.textColor(R.color.md_white_1000)

        binding.title.setBackgroundResource(
            R.drawable.black_to_transparent
        )
        binding.text.setBackgroundResource(
            R.drawable.black_to_transparent
        )
    }


    override fun onResume() {
        super.onResume()

        if (audioVolumeObserver == null) {
            audioVolumeObserver = AudioVolumeObserver(
                requireActivity()
            )
        }


        audioVolumeObserver?.register(
            AudioManager.STREAM_MUSIC,
            this
        )


        val audioManager = audioManager
        progressVolume.max = audioManager
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        progressVolume.progress = audioManager
            .getStreamVolume(AudioManager.STREAM_MUSIC)


        progressVolume.setOnSeekBarChangeListener(this)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        audioVolumeObserver?.unregister()
        _binding = null
    }

    override fun onAudioVolumeChanged(
        currentVolume: Int,
        maxVolume: Int
    ) {

        if (_binding != null) {

            progressVolume.max = maxVolume

            progressVolume.progress = currentVolume


            volumeDown.setImageResource(
                if (currentVolume == 0) R.drawable.ic_round_volume_off
                else R.drawable.ic_round_volume_down)
        }
    }


    override fun onProgressChanged(
        seekBar: SeekBar,

        i: Int,
        b: Boolean
    ) {
        val audioManager = audioManager

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            i,
            0
        )


        volumeDown.setImageResource(
            if (i == 0) R.drawable.ic_round_volume_off
            else R.drawable.ic_round_volume_down)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) { }

    override fun onStopTrackingTouch(seekBar: SeekBar) { }
}