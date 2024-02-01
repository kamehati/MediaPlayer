package com.example.elect.mediaplayer.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import com.example.elect.mediaplayer.databinding.CollapsingAppbarLayoutBinding
import com.example.elect.mediaplayer.databinding.SimpleAppbarLayoutBinding
import com.example.elect.mediaplayer.extensions.setCollapsingToolbarColor
import com.example.elect.mediaplayer.extensions.textColor
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.chrisbanes.insetter.applyInsetter

class TopAppBarLayout @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppBarLayout(context, attrs, defStyleAttr) {

    private var simpleAppbarBinding:
            SimpleAppbarLayoutBinding? = null
    private var collapsingAppBarBinding:
            CollapsingAppbarLayoutBinding? = null

    private val appbarMode = PreferenceUtil.appbarMode

    init {
        when(appbarMode) {
            0 -> {
                simpleAppbarBinding =
                    SimpleAppbarLayoutBinding.inflate(
                        LayoutInflater.from(context),
                        this,
                        true
                    )

                simpleAppbarBinding?.root?.applyInsetter {  }
            }
            1 -> {
                collapsingAppBarBinding =
                    CollapsingAppbarLayoutBinding.inflate(
                        LayoutInflater.from(context),
                        this,
                        true
                    )

            }
            else -> {
                simpleAppbarBinding =
                    SimpleAppbarLayoutBinding.inflate(
                        LayoutInflater.from(context),
                        this,
                        true
                    )

                simpleAppbarBinding?.root?.applyInsetter {  }
            }
        }


        stateListAnimator = null

        fitsSystemWindows = true
    }

    fun setTextColor(@ColorRes id: Int) {
        when(appbarMode) {
            1 -> {
                collapsingAppBarBinding?.
                appNameText?.
                textColor(id)
            }
            0 -> {
                simpleAppbarBinding?.
                appNameText?.
                textColor(id)
            }
            else -> {
                simpleAppbarBinding?.
                appNameText?.
                textColor(id)
            }
        }
    }


    fun setCollapsingColor(@ColorRes id: Int) {
        when(appbarMode) {
            1 -> {
                collapsingAppBarBinding?.
                collapsingToolbarLayout?.
                setCollapsingToolbarColor(id)
            }
            0 -> {
                return
            }
            else -> {
                return
            }
        }
    }

    val toolbar: Toolbar
        get() {
            return when(appbarMode) {
                1 -> collapsingAppBarBinding?.toolbar!!
                0 -> simpleAppbarBinding?.toolbar!!
                else -> simpleAppbarBinding?.toolbar!!
            }
        }

    val collapsingToolbar: CollapsingToolbarLayout?
        get() {
        return when(appbarMode) {
            1 -> collapsingAppBarBinding?.collapsingToolbarLayout
            0 -> null
            else -> null
        }
    }

    var collapsingTitle: String?
    get() {
        return when(appbarMode) {
            1 -> {
                collapsingAppBarBinding?.
                collapsingToolbarLayout?.
                title.toString()
            }
            0 -> {
                null
            }
            else -> {
                null
            }
        }
    }
    set(value) {
        when(appbarMode) {
            1 -> {
                collapsingAppBarBinding?.
                collapsingToolbarLayout?.
                title = value
            }
            0 -> {
                return
            }
            else -> {
                return
            }
        }
    }

    var title: String
        get() {
            return when(appbarMode) {
                1 -> {
                    collapsingAppBarBinding?.
                    appNameText?.
                    text.toString()
                }
                0 -> {
                    simpleAppbarBinding?.
                    appNameText?.
                    text.toString()
                }
                else -> {
                    simpleAppbarBinding?.
                    appNameText?.
                    text.toString()
                }
            }
        }
        set(value) {
            when(appbarMode) {
                1 -> {
                    collapsingAppBarBinding?.
                    appNameText?.
                    text = value
                }
                0 -> {
                    simpleAppbarBinding?.
                    appNameText?.
                    text = value
                }
                else -> {
                    simpleAppbarBinding?.
                    appNameText?.
                    text = value
                }
            }
        }

    val image: ImageView?
    get() {
        return if(appbarMode == 1) {
            collapsingAppBarBinding?.image
        } else {
            null
        }
    }
}