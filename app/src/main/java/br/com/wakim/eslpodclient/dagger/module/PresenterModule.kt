package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.view.PermissionRequester
import dagger.Module
import dagger.Provides

@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter(app: Application, podcastInteractor: PodcastInteractor) : PodcastListPresenter =
            PodcastListPresenter(app, podcastInteractor)

    @Provides @ActivityScope
    fun providesPlayerPresenter(app: Application, permissionRequester: PermissionRequester, podcastInteractor: PodcastInteractor) : PlayerPresenter =
            PlayerPresenter(app, permissionRequester, podcastInteractor)
}
