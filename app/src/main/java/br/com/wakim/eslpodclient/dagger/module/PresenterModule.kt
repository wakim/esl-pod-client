package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.interactor.DownloadedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.PublishSubjectItem
import br.com.wakim.eslpodclient.podcastlist.downloaded.presenter.DownloadedListPresenter
import br.com.wakim.eslpodclient.podcastlist.favorited.presenter.FavoritedListPresenter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.service.PlaylistManager
import br.com.wakim.eslpodclient.view.PermissionRequester
import dagger.Module
import dagger.Provides
import rx.subjects.PublishSubject

@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter(app: Application,
                                     publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                                     permissionRequester: PermissionRequester,
                                     playlistManager: PlaylistManager,
                                     storageInteractor: StorageInteractor,
                                     podcastInteractor: PodcastInteractor,
                                     favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor) =
            PodcastListPresenter(app, publishSubject, podcastInteractor, permissionRequester, playlistManager, storageInteractor, favoritedPodcastItemInteractor)

    @Provides @ActivityScope
    fun providesPlayerPresenter(app: Application, permissionRequester: PermissionRequester, podcastInteractor: PodcastInteractor) =
            PlayerPresenter(app, permissionRequester, podcastInteractor)

    @Provides @ActivityScope
    fun providesFavoritedListPresenter(app: Application,
                                       publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                                       permissionRequester: PermissionRequester,
                                       playlistManager: PlaylistManager,
                                       storageInteractor: StorageInteractor,
                                       favoritesInteractor: FavoritedPodcastItemInteractor) =
            FavoritedListPresenter(app, publishSubject, permissionRequester, playlistManager, storageInteractor, favoritesInteractor)

    @Provides @ActivityScope
    fun providesDownloadedListPresenter(app: Application,
                                        publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                                        permissionRequester: PermissionRequester,
                                        playlistManager: PlaylistManager,
                                        storageInteractor: StorageInteractor,
                                        interactor: DownloadedPodcastItemInteractor,
                                        favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor) =
            DownloadedListPresenter(app, publishSubject, interactor, permissionRequester, playlistManager, storageInteractor, favoritedPodcastItemInteractor)
}
