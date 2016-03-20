package br.com.wakim.eslpodclient.dagger.module

import android.app.Activity
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.PodcastItemFavoritesInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.podcastlist.favorites.presenter.FavoriteListPresenter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.view.PermissionRequester
import dagger.Module
import dagger.Provides

@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter(app: Application,
                                     permissionRequester: PermissionRequester,
                                     storageInteractor: StorageInteractor,
                                     podcastInteractor: PodcastInteractor,
                                     podcastItemFavoritesInteractor: PodcastItemFavoritesInteractor,
                                     activity: Activity) =
            PodcastListPresenter(app, podcastInteractor, permissionRequester, storageInteractor, podcastItemFavoritesInteractor, activity)

    @Provides @ActivityScope
    fun providesPlayerPresenter(app: Application, permissionRequester: PermissionRequester, podcastInteractor: PodcastInteractor) =
            PlayerPresenter(app, permissionRequester, podcastInteractor)

    @Provides @ActivityScope
    fun providesFavoriteListPresenter(app: Application,
                                      permissionRequester: PermissionRequester,
                                      storageInteractor: StorageInteractor,
                                      favoritesInteractor: PodcastItemFavoritesInteractor,
                                      podcastItemFavoritesInteractor: PodcastItemFavoritesInteractor,
                                      activity: Activity) =
            FavoriteListPresenter(app, favoritesInteractor, permissionRequester, storageInteractor, podcastItemFavoritesInteractor, activity)
}
