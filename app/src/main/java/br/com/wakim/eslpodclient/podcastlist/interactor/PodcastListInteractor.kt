package br.com.wakim.eslpodclient.podcastlist.interactor

import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

class PodcastListInteractor {
    fun getPodcasts(page : PodcastList?) : Single<PodcastList> =
            Single.create(PodcastListSubscriber(page?.nextPageUrl ?: BuildConfig.BASE_URL))
}
