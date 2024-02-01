package com.example.elect.mediaplayer.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat

class PopupBackground(
    context: Context,
    color: Int
): Drawable() {
    private var mPaddingEnd = 0

    private var mPaddingStart = 0

    private var mPaint: Paint? = null

    private val mPath = Path()

    private val mTempMatrix = Matrix()

    init {
        popupBackground(context, color)
    }

    private fun popupBackground(
        context: Context,
        color: Int
    ) {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = color
        mPaint!!.style = Paint.Style.FILL
        val resources = context.resources
        mPaddingStart = resources.getDimensionPixelOffset(
            me.zhanghai.android.fastscroll.R.dimen.afs_md2_popup_padding_start
        )
        mPaddingEnd = resources.getDimensionPixelOffset(
            me.zhanghai.android.fastscroll.R.dimen.afs_md2_popup_padding_end
        )
    }

    private fun pathArcTo(
        path: Path,
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float
    ) {
        path.arcTo(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            startAngle,
            sweepAngle,
            false
        )
    }

    override fun draw(canvas: Canvas) {
        mPaint?.let { canvas.drawPath(mPath, it) }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getOutline(outline: Outline) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !mPath.isConvex) {
            super.getOutline(outline)
            return
        }
        outline.setConvexPath(mPath)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getPadding(padding: Rect): Boolean {
        if (needMirroring()) {
            padding[mPaddingEnd, 0, mPaddingStart] = 0
        } else {
            padding[mPaddingStart, 0, mPaddingEnd] = 0
        }
        return true
    }

    override fun isAutoMirrored(): Boolean {
        return true
    }

    override fun onLayoutDirectionChanged(layoutDirection: Int): Boolean {
        updatePath()
        return true
    }

    override fun onBoundsChange(bounds: Rect) {
        updatePath()
    }

    private fun needMirroring(): Boolean {
        return DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL
    }

    private fun updatePath() {
        mPath.reset()
        val bounds = bounds
        var width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        val r = height / 2
        val sqrt2 = Math.sqrt(2.0).toFloat()
        width = Math.max(r + sqrt2 * r, width)
        pathArcTo(mPath, r, r, r, 90f, 180f)
        val o1X = width - sqrt2 * r
        pathArcTo(mPath, o1X, r, r, -90f, 45f)
        val r2 = r / 5
        val o2X = width - sqrt2 * r2
        pathArcTo(mPath, o2X, r, r2, -45f, 90f)
        pathArcTo(mPath, o1X, r, r, 45f, 45f)
        mPath.close()
        if (needMirroring()) {
            mTempMatrix.setScale(-1f, 1f, width / 2, 0f)
        } else {
            mTempMatrix.reset()
        }
        mTempMatrix.postTranslate(bounds.left.toFloat(), bounds.top.toFloat())
        mPath.transform(mTempMatrix)
    }
}