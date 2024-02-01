package com.example.elect.mediaplayer.extensions

import android.content.SharedPreferences
import android.util.Log

fun SharedPreferences.getStringOrDefault(
    key: String,
    default: String
): String{

    val a = getString(key, default)

    return getString(key, default) ?: default
}