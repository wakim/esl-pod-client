package br.com.wakim.eslpodclient.android.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class BottomSpacingItemDecoration(var bottomSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        val position = parent?.getChildAdapterPosition(view)
        val isLast = (position == (parent?.adapter?.itemCount?.minus(1)))

        if (!isLast) {
            return
        }

        outRect?.bottom = bottomSpacing
    }
}