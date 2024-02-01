package com.example.elect.mediaplayer.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView


class CustomBottomNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): BottomNavigationView(
    context, attrs, defStyleAttr
){
    init {

        labelVisibilityMode = LABEL_VISIBILITY_SELECTED
    }
}