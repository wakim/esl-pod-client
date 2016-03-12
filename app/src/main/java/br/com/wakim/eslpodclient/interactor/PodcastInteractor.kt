package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single

open class PodcastInteractor(val storageInteractor: StorageInteractor) {
    open fun getPodcasts(nextPageUrl: String?) : Single<PodcastList> =
            Single.create(PodcastListOnSubscribe(storageInteractor, nextPageUrl ?: BuildConfig.BASE_URL))

    open fun getPodcastDetail(podcastItem : PodcastItem) : Single<PodcastItemDetail> =
            Single.create(PodcastDetailOnSubscribe(podcastItem, BuildConfig.DETAIL_URL + "?issue_id=${podcastItem.remoteId}"))
}
