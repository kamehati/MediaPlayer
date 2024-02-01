package com.example.elect.mediaplayer.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.Px
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import com.example.elect.mediaplayer.extensions.applyBottomInsets

class InsetsCoordinatorLayout@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(
    context,
    attrs,
    defStyleAttr
) {


    fun updateMargin(
        bottom: Float
    ){
        translationY = bottom
    }
}