package br.com.wakim.eslpodclient.podcastlist.downloaded.presenter

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.DownloadedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PublishSubjectItem
import br.com.wakim.eslpodclient.podcastlist.downloaded.view.DownloadedListView
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.service.PlaylistManager
import br.com.wakim.eslpodclient.service.StorageService
import br.com.wakim.eslpodclient.view.PermissionRequester
import rx.subjects.PublishSubject

class DownloadedListPresenter: PodcastListPresenter {

    constructor(app: Application,
                publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                downloadedPodcastItemInteractor: DownloadedPodcastItemInteractor,
                permissionRequester: PermissionRequester,
                playlistManager: PlaylistManager,
                storageInteractor: StorageInteractor,
                favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor) :
    super(app, publishSubject, downloadedPodcastItemInteractor, permissionRequester, playlistManager, storageInteractor, favoritedPodcastItemInteractor)

    var storageService: StorageService? = null
        set(value) {
            setSynchronizing(value?.synchronizing ?: false)
            field = value
        }

    override fun onStart() {
        super.onStart()

        addSubscription {
            publishSubject
                    .ofIOToMainThread()
                    .filter { it.type == PublishSubjectItem.PODCAST_SYNC_TYPE || it.type == PublishSubjectItem.PODCAST_SYNC_ENDED_TYPE }
                    .subscribe { item ->
                        if (item.type == PublishSubjectItem.PODCAST_SYNC_TYPE) {
                            view?.addItem(item.t as PodcastItem)
                        } else {
                            setSynchronizing(false)
                        }
                    }
        }
    }

    override fun onRefresh() {
        items.clear()

        nextPageToken = null

        loadNextPage()
    }

    fun synchronize() {
        setSynchronizing(true)
        storageService?.synchronize()
    }

    fun setSynchronizing(synchronizing: Boolean) {
        (view as DownloadedListView).setSynchronizeMenuVisible(!synchronizing)
    }
}