package br.com.wakim.eslpodclient.podcastlist.favorited.presenter

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.PublishSubjectItem
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.service.PlaylistManager
import br.com.wakim.eslpodclient.view.PermissionRequester
import rx.subjects.PublishSubject

class FavoritedListPresenter : PodcastListPresenter {

    constructor(app: Application,
                publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                permissionRequester: PermissionRequester,
                playlistManager: PlaylistManager,
                storageInteractor: StorageInteractor,
                favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor) :
    super(app, publishSubject, favoritedPodcastItemInteractor, permissionRequester, playlistManager, storageInteractor, favoritedPodcastItemInteractor)

    override fun onRefresh() {
        items.clear()

        nextPageToken = null

        loadNextPage()
    }
}