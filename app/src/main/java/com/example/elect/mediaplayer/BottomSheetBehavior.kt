package com.example.elect.mediaplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetBehavior<V: View> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): BottomSheetBehavior<V>(
    context,
    attrs
) {

    private var allowDragging = true


    fun setAllowDragging(allowDragging: Boolean){
        this.allowDragging = allowDragging
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        if(!allowDragging){
            return false
        }

        return super.onInterceptTouchEvent(parent, child, event)
    }
}