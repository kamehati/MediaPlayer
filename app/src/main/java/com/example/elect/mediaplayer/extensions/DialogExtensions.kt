package com.example.elect.mediaplayer.extensions

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.elect.mediaplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun DialogFragment.materialDialog(title: Int)
: MaterialAlertDialogBuilder{
    return MaterialAlertDialogBuilder(
        requireContext(),
        R.style.MaterialAlertDialogTheme
    ).setTitle(title)
        .setBackground(

            this.context?.let { ContextCompat.getDrawable(it, R.drawable.popup_background) }
        )
}


fun AlertDialog.colorButtons(): AlertDialog {

    setOnShowListener {

        getButton(AlertDialog.BUTTON_POSITIVE).textColor(R.color.md_white_1000)
        getButton(AlertDialog.BUTTON_NEGATIVE).textColor(R.color.md_white_1000)
        getButton(AlertDialog.BUTTON_NEUTRAL).textColor(R.color.md_white_1000)
    }
    return this
}