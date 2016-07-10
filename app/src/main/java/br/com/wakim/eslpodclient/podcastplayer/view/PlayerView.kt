package br.com.wakim.eslpodclient.podcastplayer.view

import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.view.View

interface PlayerView : View {
    fun showPlayButton()
    fun showLoadingButton()
    fun showPauseButton()

    fun setMaxProgress(duration: Int)

    fun getProgressValue(): Int
    fun setProgressValue(position: Int)
    fun setSecondaryProgressValue(position: Int)

    fun setSeekEnabled(enabled: Boolean)

    fun setStreamType(@PodcastItem.StreamType streamType: Long)

    fun setLoading(loading: Boolean)

    fun bindPodcastItem(podcastItem: PodcastItem)
    fun bindPodcastDetail(podcastItemDetail: PodcastItemDetail)

    fun setVisible()
}