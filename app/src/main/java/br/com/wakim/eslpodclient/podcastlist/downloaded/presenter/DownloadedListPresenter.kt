package br.com.wakim.eslpodclient.podcastlist.downloaded.presenter

import android.app.Activity
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.podcastlist.downloaded.view.DownloadedListView
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.view.PermissionRequester

class DownloadedListPresenter: PodcastListPresenter {

    constructor(app: Application,
                interactor: PodcastInteractor,
                permissionRequester: PermissionRequester,
                storageInteractor: StorageInteractor,
                favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor,
                baseActivity: Activity) :
    super(app, interactor, permissionRequester, storageInteractor, favoritedPodcastItemInteractor, baseActivity)

    var downloadListView: DownloadedListView? = null

    fun synchronize() {
        downloadListView?.let {
            it.setSynchronizeMenuVisibible(false)
            it.showAppBarLoading(true)
        }

        storageInteractor.synchronizeDownloads()
                .ofIOToMainThread()
                .subscribe { result ->
                    downloadListView?.let {
                        it.setSynchronizeMenuVisibible(true)
                        it.showAppBarLoading(false)
                    }
                }
    }
}