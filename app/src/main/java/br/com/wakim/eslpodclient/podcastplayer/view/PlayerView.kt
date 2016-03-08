package br.com.wakim.eslpodclient.podcastplayer.view

import android.support.annotation.StringRes
import br.com.wakim.eslpodclient.view.View

interface PlayerView : View {
    fun showPlayButton()
    fun showPauseButton()

    fun setMaxProgress(duration: Int)

    fun getProgressValue(): Int
    fun setProgressValue(position: Int)

    fun showMessage(@StringRes messageResId: Int)
}