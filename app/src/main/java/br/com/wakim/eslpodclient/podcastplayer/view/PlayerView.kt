package br.com.wakim.eslpodclient.podcastplayer.view

import android.support.annotation.StringRes
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.view.View

interface PlayerView : View {
    fun showPlayButton()
    fun showPauseButton()

    fun setMaxProgress(duration: Int)

    fun setMaxAvailableProgress(available: Int)

    fun getProgressValue(): Int
    fun setProgressValue(position: Int)

    fun showMessage(@StringRes messageResId: Int)

    fun setLoading(loading: Boolean)

    fun setPodcastDetail(podcastItemDetail: PodcastItemDetail)
}