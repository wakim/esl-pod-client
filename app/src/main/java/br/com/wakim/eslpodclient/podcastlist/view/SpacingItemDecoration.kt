package br.com.wakim.eslpodclient.podcastlist.view

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpacingItemDecoration(val columns: Int = 1, val spacingLeft: Int = 0, val spacingTop: Int = 0, val spacingRight: Int = 0, val spacingBottom: Int = 0, val includeEdge: Boolean = false) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % columns

        if (includeEdge) {
            outRect.left = spacingLeft - column * spacingLeft / column // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacingRight / column // (column + 1) * ((1f / spanCount) * spacing)

            if (position < columns) { // top edge
                outRect.top = spacingTop
            }

            outRect.bottom = spacingBottom // item bottom
        } else {
            outRect.left = column * spacingLeft / columns // column * ((1f / spanCount) * spacing)
            outRect.right = spacingRight - (column + 1) * spacingRight / columns // spacing - (column + 1) * ((1f /    spanCount) * spacing)

            if (position >= columns) {
                outRect.top = spacingTop // item top
            }
        }
    }
}