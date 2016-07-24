package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.android.service.PlaylistManager
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.data.interactor.DownloadedPodcastItemInteractor
import br.com.wakim.eslpodclient.data.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.data.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.data.interactor.StorageInteractor
import br.com.wakim.eslpodclient.data.model.PublishSubjectItem
import br.com.wakim.eslpodclient.ui.podcastlist.downloaded.presenter.DownloadedListPresenter
import br.com.wakim.eslpodclient.ui.podcastlist.favorited.presenter.FavoritedListPresenter
import br.com.wakim.eslpodclient.ui.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.ui.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.ui.view.PermissionRequester
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
