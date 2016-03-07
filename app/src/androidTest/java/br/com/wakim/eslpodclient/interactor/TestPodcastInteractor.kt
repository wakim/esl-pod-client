package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

class TestPodcastInteractor : PodcastInteractor() {
    override fun getPodcasts(page: PodcastList?): Single<PodcastList> {
        return Single.just(PodcastList())
    }
}