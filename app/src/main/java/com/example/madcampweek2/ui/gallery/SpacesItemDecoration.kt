package com.example.madcampweek2.ui.gallery

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val width: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
        val column = position % spanCount
        val space = width / (spanCount + 1)
        outRect.left = space - column * space / spanCount
        outRect.right = (column + 1) * space / spanCount
        if(position < spanCount) outRect.top = space
        outRect.bottom = space
    }
}