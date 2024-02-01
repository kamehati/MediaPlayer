package com.example.elect.mediaplayer.transform

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CarousalPagerTransformer(context: Context)

    : ViewPager2.PageTransformer {

    private val maxTranslateOffsetX: Int
    private var viewPager2: RecyclerView? = null

    init {

        this.maxTranslateOffsetX = dp2px(context, 180f)
    }


    override fun transformPage(
        view: View,
        position: Float
    ) {
        if (viewPager2 == null) {

            viewPager2 = view.parent as RecyclerView
        }


        val leftInScreen = view.left - viewPager2!!.scrollX

        val centerXInviewPager2 = leftInScreen + view.measuredWidth / 2

        val offsetX = centerXInviewPager2 - viewPager2!!.measuredWidth / 2
        val offsetRate = offsetX.toFloat() * 0.30f / viewPager2!!.measuredWidth

        val scaleFactor = 1 - abs(offsetRate)

        if (scaleFactor > 0) {

            view.scaleX = scaleFactor
            view.scaleY = scaleFactor

            view.translationX = -maxTranslateOffsetX * offsetRate
        }
    }


    private fun dp2px(
        context: Context,
        dipValue: Float
    ): Int {

        val m = context.resources.displayMetrics.density

        return (dipValue * m + 0.5f).toInt()
    }

}