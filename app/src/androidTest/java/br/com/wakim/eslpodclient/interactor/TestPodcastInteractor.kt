package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

class TestPodcastInteractor(storageInteractor: StorageInteractor) : PodcastInteractor(storageInteractor) {
    override fun getPodcasts(page: PodcastList?): Single<PodcastList> {
        return Single.just(PodcastList())
    }
}