package com.example.elect.mediaplayer.util

import android.os.Build

object BuildUtil {

    fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun isPPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}