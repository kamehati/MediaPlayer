package com.example.elect.mediaplayer.interfaces

import com.afollestad.materialcab.attached.AttachedCab

interface ICabHolder {

    fun openCab(menuRes: Int, callback: ICabCallback): AttachedCab
}
