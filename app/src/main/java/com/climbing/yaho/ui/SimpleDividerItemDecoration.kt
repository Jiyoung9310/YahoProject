package com.climbing.yaho.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView


class SimpleDividerItemDecoration(
    context: Context,
    private val orientation: Int = RecyclerView.VERTICAL,
    private val startIndex: Int = 0,
    private val space: Int = 0
) : DividerItemDecoration(context, orientation) {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (startIndex > 0) {
            if (parent.getChildAdapterPosition(view) != startIndex - 1) {
                outRect.bottom = space
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || drawable == null) {
            return
        }
        if (orientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft + space
            right = parent.width - parent.paddingRight - space
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = space
            right = parent.width - space
        }
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val bounds = Rect()
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + Math.round(child.translationY)
            val top = bottom - (drawable?.intrinsicHeight ?: 0)
            drawable?.setBounds(left, top, right, bottom)
            drawable?.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop + space
            bottom = parent.height - parent.paddingBottom - space
            canvas.clipRect(
                parent.paddingLeft, top,
                parent.width - parent.paddingRight, bottom
            )
        } else {
            top = space
            bottom = parent.height - space
        }
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val bounds = Rect()
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, bounds)
            val right = bounds.right + Math.round(child.translationX)
            val left = right - (drawable?.intrinsicWidth ?: 0)
            drawable?.setBounds(left, top, right, bottom)
            drawable?.draw(canvas)
        }
        canvas.restore()
    }

}