package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
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

class MediaLoudnessDialog (
    private val playerActivity: PlayerActivity
) : DialogFragment() {

    private lateinit var loudnessEnhancer: LoudnessEnhancer

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val binding = DialogSliderBinding.inflate(layoutInflater)

        loudnessEnhancer = LoudnessEnhancer(PlayerRemote.audioSessionId)
        loudnessEnhancer.enabled = true

        binding.slider.apply {
            addOnChangeListener(
                Slider.OnChangeListener { _, value, _ ->
                    val s = playerActivity.getString(
                        R.string.loudness_property,
                        (value - 50F).toString()
                    )
                    binding.value.text = s


                    loudnessEnhancer.setTargetGain(

                        (value - 50F).toInt() * 50
                    )

                    PreferenceUtil.mediaLoudNess = value.toInt()
                }
            )

            valueFrom = 0F
            valueTo = 100F
            value = PreferenceUtil.mediaLoudNess.toFloat()
            seekBarColor(R.color.md_blue_500, R.color.md_blue_grey_800)
        }


        return materialDialog(R.string.action_loudness)
            .setPositiveButton(R.string.reset){ _, _ ->
                binding.slider.value = 50F
                val value = binding.slider.value
                val s = playerActivity.getString(
                    R.string.loudness_property,
                    (value - 50F).toString()
                )
                binding.value.text = s
                loudnessEnhancer.setTargetGain(
                    (value - 50F).toInt() * 50
                )

                PreferenceUtil.mediaLoudNess = value.toInt()
            }
            .setNeutralButton(android.R.string.cancel){ dialog, _ ->
                dialog.dismiss()
            }
            .setView(binding.root)
            .create()
            .colorButtons()
    }

    companion object {
        fun newInstance(
            playerActivity: PlayerActivity
        ): MediaLoudnessDialog {
            return MediaLoudnessDialog(playerActivity)
        }
    }
}