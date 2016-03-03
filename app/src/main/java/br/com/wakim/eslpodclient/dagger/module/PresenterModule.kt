package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.podcastlist.interactor.PodcastListInteractor
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import dagger.Module
import dagger.Provides

@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter(podcastListInteractor: PodcastListInteractor) : PodcastListPresenter =
            PodcastListPresenter(podcastListInteractor)
}
