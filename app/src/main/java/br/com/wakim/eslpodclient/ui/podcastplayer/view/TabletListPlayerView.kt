package br.com.wakim.eslpodclient.ui.podcastplayer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

class TabletListPlayerView: ListPlayerView {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun setupBehaviorCallback() { }

    override fun setVisible() {
        visibility = View.VISIBLE
    }

    override fun isVisible() = visibility == View.VISIBLE

    override fun hide() {
        visibility = View.GONE
    }
}