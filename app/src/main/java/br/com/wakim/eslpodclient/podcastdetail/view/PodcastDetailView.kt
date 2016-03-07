package br.com.wakim.eslpodclient.podcastdetail.view

import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.view.View

interface PodcastDetailView : View {
    fun getPodcastItemParameter() : PodcastItem
    fun setLoading(loading: Boolean)

    fun setPodcastDetail(podcastItemDetail: PodcastItemDetail)
}