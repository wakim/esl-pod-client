package br.com.wakim.eslpodclient.podcastplayer.view

import br.com.wakim.eslpodclient.model.PodcastItem

interface QueueManager {
    fun getNext(podcastItem: PodcastItem) : PodcastItem?
    fun getPrevious(podcastItem: PodcastItem) : PodcastItem?
}