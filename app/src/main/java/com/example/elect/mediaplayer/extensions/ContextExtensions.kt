package com.example.elect.mediaplayer.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.elect.mediaplayer.util.BuildUtil

fun Context.hasPermission(
    permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
): Boolean{

    if(BuildUtil.isMarshmallowPlus()) {

        val base = ContextCompat.checkSelfPermission(this, permission)
        return base == PackageManager.PERMISSION_GRANTED
    }
    return true
}

fun Context.hasBlueToothPermission(
    permission: String = Manifest.permission.BLUETOOTH
): Boolean {
    if(BuildUtil.isMarshmallowPlus()) {
        val base = ContextCompat.checkSelfPermission(this, permission)
        return base == PackageManager.PERMISSION_GRANTED
    }
    return true
}