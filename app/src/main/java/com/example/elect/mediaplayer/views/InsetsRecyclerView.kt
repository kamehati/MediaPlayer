package com.example.elect.mediaplayer.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.extensions.applyBottomInsets

class InsetsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(
    context,
    attrs,
    defStyleAttr
){
    init {

        applyBottomInsets()
    }

    fun updatePadding(
        @Px left: Int = paddingLeft,
        @Px top: Int = paddingTop,
        @Px right: Int = paddingRight,
        @Px bottom: Int = paddingBottom
    ){

        setPadding(left, top, right, bottom)

        applyBottomInsets()
    }
}