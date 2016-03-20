package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

class TestPodcastInteractor(private val podcastDbInteractor: PodcastDbInteractor, private val app: Application) : PodcastInteractor(podcastDbInteractor, app) {
    override fun getPodcasts(nextPageUrl: String?): Single<PodcastList> {
        return Single.just(PodcastList())
    }
}