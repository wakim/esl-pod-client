package br.com.wakim.eslpodclient.podcastlist.view

import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.view.View
import java.util.*

interface PodcastListView : View {
    var hasMore : Boolean

    fun addItems(list: ArrayList<PodcastItem>)
    fun setLoading(loading : Boolean)

    fun remove(podcastItem: PodcastItem)

    fun setItems(list: ArrayList<PodcastItem>)

    fun addItem(podcastItem: PodcastItem)

    fun share(text: String?)

    fun openUrlOnBrowser(url: String)
}