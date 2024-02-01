package com.example.elect.mediaplayer.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.example.elect.mediaplayer.helper.TintHelper
import com.example.elect.mediaplayer.views.PopupBackground
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.PopupStyles


fun BottomNavigationView.backgroundTintListColor(@ColorRes id: Int) {
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun BottomNavigationView.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}


fun BottomNavigationView.itemIconColor(@ColorRes id: Int) {
    itemIconTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun BottomNavigationView.itemTextColor(@ColorRes id: Int) {
    itemTextColor = ColorStateList.valueOf(ContextCompat.getColor(context,id))
    itemTextAppearanceInactive
}


fun BottomNavigationView.itemRippleColor(@ColorRes id: Int) {
    itemRippleColor = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun BottomNavigationView.activeItemColor(@ColorRes id: Int) {
    itemActiveIndicatorColor = ColorStateList.valueOf(ContextCompat.getColor(context,id))
    isItemActiveIndicatorEnabled
}


fun FrameLayout.backGroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}


fun AppCompatImageView.backGroundColor(@ColorRes id: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun MaterialButton.backgroundColor(@ColorRes id: Int) {
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun MaterialButton.textColor(@ColorRes id: Int) {
    setTextColor(ContextCompat.getColor(context,id))
}


fun MaterialButton.strokeColor(@ColorRes id: Int) {
    setStrokeColorResource(id)
}


fun MaterialButton.iconColor(@ColorRes id: Int) {
    setIconTintResource(id)
}


fun MaterialButton.rippleColor(@ColorRes id: Int) {
    setRippleColorResource(id)
}

fun Button.textColor(@ColorRes id: Int) {
    setTextColor(ContextCompat.getColor(context,id))
}


fun Toolbar.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}


fun MaterialTextView.textColor(@ColorRes id: Int) {
    setTextColor(ContextCompat.getColor(context,id))
}


fun CollapsingToolbarLayout.setCollapsingToolbarColor(@ColorRes id: Int) {
    setContentScrimColor(ContextCompat.getColor(context,id))
}


fun MaterialCardView.strokeColor(@ColorRes id: Int) {
    setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context,id)))
}


fun CoordinatorLayout.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}


fun TabLayout.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}


fun TabLayout.selectedTabIndicatorColor(@ColorRes id: Int) {
    setSelectedTabIndicatorColor(
        ContextCompat.getColor(context,id)
    )
}


fun TabLayout.tabTextColor(
    @ColorRes unSelected: Int,
    @ColorRes selected: Int
) {
    setTabTextColors(
        ContextCompat.getColor(context,unSelected),
        ContextCompat.getColor(context,selected)
    )
}


fun TabLayout.rippleColor(@ColorRes id: Int) {
    setTabRippleColorResource(id)
}


fun FastScrollerBuilder.textAndBackgroundColor(
    context: Context,
    @ColorRes textColor: Int,
    @ColorRes backgroundColor: Int
) {

    val backgroundColor = ContextCompat.getColor(
        context, backgroundColor)

    val textColor = ContextCompat.getColor(
        context, textColor)


    setPopupStyle { popupText ->

        PopupStyles.MD2.accept(popupText)

        popupText.background = PopupBackground(context, backgroundColor)

        popupText.setTextColor(textColor)
    }
}


fun FastScrollerBuilder.scrollerColor(
    context: Context,
    @ColorRes id: Int
) {

    val scrollerColor = ContextCompat.getColor(context,id)

    TintHelper.createTintedDrawable(
        context,

        me.zhanghai.android.fastscroll.R.drawable.afs_md2_thumb,
        scrollerColor
    )?.let {
        setThumbDrawable(

            it
        )
    }
}

fun View.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context, id))
}


fun SeekBar.seekbarColor(
    @ColorRes tintColor: Int,
    @ColorRes progressColor: Int
) {
    thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context,tintColor))

    progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context,tintColor))

    progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,progressColor))
}


fun TextView.textColor(@ColorRes id: Int) {
    setTextColor(ContextCompat.getColor(context,id))
}


fun FloatingActionButton.backgroundColor(
    @ColorRes backgroundColor: Int,
    @ColorRes iconColor: Int
) {

    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,backgroundColor))

    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context,iconColor))
}


fun AppCompatImageView.iconColor(@ColorRes id: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}


fun DefaultTimeBar.seekbarColor(
    @ColorRes tintColor: Int,
    @ColorRes progressColor: Int
) {

    setScrubberColor(ContextCompat.getColor(context,tintColor))

    setPlayedColor(ContextCompat.getColor(context,tintColor))

    setUnplayedColor(ContextCompat.getColor(context,progressColor))
    setBufferedColor(ContextCompat.getColor(context,progressColor))


}

fun Snackbar.backgroundColor(@ColorRes id: Int): Snackbar {
    setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,id)))
    return this
}

fun Snackbar.textColor(@ColorRes id: Int): Snackbar {
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context,id)))
    return this
}

fun FragmentActivity.statusBarColor(@ColorRes id: Int) {
    window.statusBarColor = ContextCompat.getColor(baseContext,id)
}

fun FragmentActivity.navigationBarColor(@ColorRes id: Int) {
    window.navigationBarColor = ContextCompat.getColor(baseContext,id)
}

fun ImageButton.iconColor(@ColorRes id: Int) {
    setColorFilter(
        ContextCompat.getColor(context, id),
        PorterDuff.Mode.SRC_IN
    )
}

fun FragmentContainerView.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}

fun FrameLayout.backgroundColor(@ColorRes id: Int) {
    setBackgroundColor(ContextCompat.getColor(context,id))
}

fun ImageView.iconColor(@ColorRes id: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context,id))
}

fun Slider.seekBarColor(
    @ColorRes tintColor: Int,
    @ColorRes progressColor: Int
) {

    thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context,tintColor))

    trackTintList = ColorStateList.valueOf(ContextCompat.getColor(context,progressColor))
}