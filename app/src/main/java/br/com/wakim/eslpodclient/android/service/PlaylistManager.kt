package br.com.wakim.eslpodclient.android.service

import br.com.wakim.eslpodclient.data.model.PodcastItem
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistManager {
    private var items : List<PodcastItem> = listOf()

    @Inject
    constructor()

    fun setItems(items: ArrayList<PodcastItem>) {
        this.items = items
    }

    fun nextOrNull(podcastItem: PodcastItem) : PodcastItem? {
        val indexOf = items.indexOf(podcastItem)

        return if (indexOf == -1) null else items.getOrNull(indexOf + 1)
    }

    fun previousOrNull(podcastItem: PodcastItem) : PodcastItem? {
        val indexOf = items.indexOf(podcastItem)

        return if (indexOf == -1) null else items.getOrNull(indexOf - 1)
    }

    fun clearItems() {
        items = listOf()
    }
}