package com.example.elect.mediaplayer.extensions

import android.app.Activity
import android.content.Intent
import androidx.annotation.DimenRes

fun Activity.dip(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}


inline fun <reified T : Any> Intent.extra(
    key: String,
    default: T? = null
) = lazy {
    val value = extras?.get(key)
    if (value is T) value else default
}