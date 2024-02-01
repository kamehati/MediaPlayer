package com.example.elect.mediaplayer.extensions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.chrisbanes.insetter.applyInsetter

fun View.applyBottomInsets() {

    val initialPadding = recordInitialPaddingForView(this)

    ViewCompat.setOnApplyWindowInsetsListener(
        this
    ){v: View, windowInsets: WindowInsetsCompat ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars()
        )

        v.updatePadding(
            bottom = initialPadding.bottom + insets.bottom
        )

        windowInsets
    }

    requestApplyInsetsWhenAttached()
}


fun View.requestApplyInsetsWhenAttached(){
    if(isAttachedToWindow) {

        requestApplyInsets()
    }
    else{
        addOnAttachStateChangeListener(

            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    v.requestApplyInsets()
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            }
        )
    }
}


data class InitialPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft,
    view.paddingTop,
    view.paddingRight,
    view.paddingBottom
)


fun View.drawAboveSystemBars(onlyPortrait: Boolean = true) {

    applyInsetter {
        type(navigationBars = true) {
            margin()
        }
    }
}

fun BottomSheetBehavior<*>.peekHeightAnimate(value: Int): Animator {
    return ObjectAnimator.ofInt(
        this,
        "peekHeight",
        value
    ).apply {
            duration = 500
            start()
        }
}

fun View.translateYAnimate(value: Float): Animator {
    return ObjectAnimator.ofFloat(
        this,
        "translationY",
        value
    ).apply {
        duration = 500
        doOnStart {
            show()
            bringToFront()
        }
        doOnEnd {
            if (value != 0f) {
                hide()
            } else {
                show()
            }
        }
        start()
    }
}

fun View.show() {
    isVisible = true
}

fun View.hide() {
    isVisible = false
}