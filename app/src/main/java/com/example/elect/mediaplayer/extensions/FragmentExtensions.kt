package com.example.elect.mediaplayer.extensions

import android.content.Context
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

fun Fragment.getIntRes(@IntegerRes int: Int): Int{
    return resources.getInteger(int)
}

fun Context.getIntRes(@IntegerRes int: Int): Int{
    return resources.getInteger(int)
}

fun AppCompatActivity.currentFragment(navHostId: Int)
: Fragment? {
    val navHostFragment = supportFragmentManager
        .findFragmentById(navHostId) as NavHostFragment


    return navHostFragment
        .childFragmentManager
        .fragments
        .firstOrNull()
}

@Suppress("UNCHECKED_CAST")
fun <T> AppCompatActivity.whichFragment(@IdRes id: Int): T {
    return supportFragmentManager.findFragmentById(id) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> Fragment.whichFragment(@IdRes id: Int): T {
    return childFragmentManager.findFragmentById(id) as T
}

fun Fragment.dip(@DimenRes id:Int): Int{
    return resources.getDimensionPixelSize(id)
}


inline fun <reified T: Any> Fragment.extraNotNull(
    key: String, default: T? = null
) = lazy {

    val value = arguments?.get(key)

    requireNotNull(
        if(value is T) value
    else default
    ) { key }
}

inline fun <reified T : Any> Fragment.extra(
    key: String, default: T? = null
) = lazy {

    val value = arguments?.get(key)
    if (value is T) value else default
}