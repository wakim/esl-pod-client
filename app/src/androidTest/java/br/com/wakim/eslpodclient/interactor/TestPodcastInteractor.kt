package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

class TestPodcastInteractor(storageInteractor: StorageInteractor) : PodcastInteractor() {
    override fun getPodcasts(nextPageUrl: String?): Single<PodcastList> {
        return Single.just(PodcastList())
    }
}