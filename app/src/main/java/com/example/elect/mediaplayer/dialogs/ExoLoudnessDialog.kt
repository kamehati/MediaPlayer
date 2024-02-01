package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.databinding.DialogSliderBinding
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.extensions.seekBarColor
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.slider.Slider

class ExoLoudnessDialog(
    private val playerActivity: PlayerActivity,
    private val audioSessionId: Int
) : DialogFragment() {

    private lateinit var exoPlayerLoudnessEnhancer: LoudnessEnhancer
    private lateinit var mediaPlayerLoudnessEnhancer: LoudnessEnhancer

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val binding = DialogSliderBinding.inflate(layoutInflater)

        exoPlayerLoudnessEnhancer = LoudnessEnhancer(audioSessionId)
        mediaPlayerLoudnessEnhancer = LoudnessEnhancer(PlayerRemote.audioSessionId)
        exoPlayerLoudnessEnhancer.enabled = true
        mediaPlayerLoudnessEnhancer.enabled = true

        binding.slider.apply {
            addOnChangeListener(
                Slider.OnChangeListener { _, value, _ ->
                    val s = playerActivity.getString(
                        R.string.brightness_property,
                        (value - 50F).toString()
                    )
                    binding.value.text = s

                    exoPlayerLoudnessEnhancer.setTargetGain(

                        (value - 50F).toInt() * 50
                    )

                    PreferenceUtil.exoLoudNess = value.toInt()
                    PreferenceUtil.mediaLoudNess = value.toInt()
                }
            )

            valueFrom = 0F
            valueTo = 100F
            value = PreferenceUtil.exoLoudNess.toFloat()
            seekBarColor(R.color.md_blue_500, R.color.md_blue_grey_800)
        }

        return materialDialog(R.string.action_loudness)
            .setPositiveButton(R.string.reset){ _ , _ ->
                binding.slider.value = 50F
                val value = binding.slider.value
                val s = playerActivity.getString(
                    R.string.brightness_property,
                    (value - 50F).toString()
                )
                binding.value.text = s

                exoPlayerLoudnessEnhancer.setTargetGain(
                    (value - 50F).toInt() * 50
                )

                PreferenceUtil.exoLoudNess = value.toInt()
                PreferenceUtil.mediaLoudNess = value.toInt()
            }
            .setNeutralButton(android.R.string.cancel){ dialog , _ ->
                dialog.dismiss()
            }
            .setView(binding.root)

            .setBackground(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_blue_grey_transparent_900
                    )
                )
            )
            .create()
            .colorButtons()
    }

    companion object {
        fun newInstance(
            playerActivity: PlayerActivity,
            audioSessionId: Int
        ): ExoLoudnessDialog {
            return ExoLoudnessDialog(
                playerActivity,
                audioSessionId
            )
        }
    }
}