package com.example.elect.mediaplayer.helper

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

object TintHelper {


    @CheckResult
    fun createTintedDrawable(
        context: Context?,
        @DrawableRes res: Int,
        @ColorInt color: Int
    ): Drawable? {

        val drawable = ContextCompat
            .getDrawable(context!!, res)

        return createTintedDrawable(drawable, color)
    }


    @CheckResult
    fun createTintedDrawable(
        drawable: Drawable?,
        @ColorInt color: Int
    ): Drawable? {
        var drawable = drawable ?: return null


        drawable = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTintMode(
            drawable,
            PorterDuff.Mode.SRC_IN
        )
        DrawableCompat.setTint(drawable, color)
        return drawable
    }
}