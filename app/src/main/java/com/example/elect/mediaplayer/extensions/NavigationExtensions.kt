package com.example.elect.mediaplayer.extensions

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment


fun AppCompatActivity.findNavController(@IdRes id: Int)
: NavController {
    val fragment = supportFragmentManager
        .findFragmentById(id) as NavHostFragment
    return fragment.navController
}