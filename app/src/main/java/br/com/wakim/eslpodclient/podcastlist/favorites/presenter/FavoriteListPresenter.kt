package br.com.wakim.eslpodclient.podcastlist.favorites.presenter

import android.app.Activity
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.PodcastItemFavoritesInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.view.PermissionRequester

class FavoriteListPresenter: PodcastListPresenter {

    constructor(app: Application,
                interactor: PodcastInteractor,
                permissionRequester: PermissionRequester,
                storageInteractor: StorageInteractor,
                podcastItemFavoritesInteractor: PodcastItemFavoritesInteractor,
                baseActivity: Activity) :
    super(app, interactor, permissionRequester, storageInteractor, podcastItemFavoritesInteractor, baseActivity)
}