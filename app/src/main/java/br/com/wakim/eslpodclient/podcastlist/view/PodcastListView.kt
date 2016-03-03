package br.com.wakim.eslpodclient.podcastlist.view

import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.view.View
import java.util.*

interface PodcastListView : View {
    fun addItems(list: ArrayList<PodcastItem>)
}