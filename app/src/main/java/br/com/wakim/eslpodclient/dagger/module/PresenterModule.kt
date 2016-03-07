package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.podcastdetail.presenter.PodcastDetailPresenter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import dagger.Module
import dagger.Provides

@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter(podcastInteractor: PodcastInteractor) : PodcastListPresenter =
            PodcastListPresenter(podcastInteractor)

    @Provides @ActivityScope
    fun providesPodcastDetailPresenter(podcastInteractor: PodcastInteractor, app : Application) : PodcastDetailPresenter =
            PodcastDetailPresenter(podcastInteractor, app)

    @Provides @ActivityScope
    fun providesPlayerPresenter(app: Application) : PlayerPresenter = PlayerPresenter(app)
}
