package com.example.elect.mediaplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.databinding.DialogSliderBinding
import com.example.elect.mediaplayer.extensions.colorButtons
import com.example.elect.mediaplayer.extensions.materialDialog
import com.example.elect.mediaplayer.extensions.seekBarColor
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.slider.Slider


class BrightnessDialog(
    private val playerActivity: PlayerActivity
) : DialogFragment() {
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val binding = DialogSliderBinding.inflate(layoutInflater)
        binding.slider.apply {
            addOnChangeListener(
                Slider.OnChangeListener { _, value, _ ->
                    val s = playerActivity.getString(
                        R.string.brightness_property,
                        value.toString()
                    )
                    binding.value.text = s
                    setScreenBrightness(value.toInt())

                    PreferenceUtil.brightNess = value.toInt()
                }
            )

            value = PreferenceUtil.brightNess.toFloat()
            seekBarColor(R.color.md_blue_500, R.color.md_blue_grey_800)
        }

        return materialDialog(R.string.action_brightness)
            .setPositiveButton(R.string.reset){ _, _ ->
                binding.slider.value = 0F
                val value = binding.slider.value
                val s = playerActivity.getString(
                    R.string.brightness_property,
                    value.toString()
                )
                binding.value.text = s
                setScreenBrightness(value.toInt())

                PreferenceUtil.brightNess = value.toInt()
            }
            .setNeutralButton(android.R.string.cancel){ dialog, _ ->
                dialog.dismiss()
            }
            .setView(binding.root)
            .create()
            .colorButtons()
    }

    private fun setScreenBrightness(value: Int){
        PreferenceUtil.brightNess = value

        val d = 1.0f/100
        val lp = playerActivity.window.attributes
        lp.screenBrightness = d * value
        playerActivity.window.attributes = lp
    }

    companion object {
        fun newInstance(
            playerActivity: PlayerActivity
        ): BrightnessDialog {
            return BrightnessDialog(playerActivity)
        }
    }
}