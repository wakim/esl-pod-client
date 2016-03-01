package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import dagger.Module
import dagger.Provides

/**
 * Created by wakim on 2/29/16.
 */
@Module
class PresenterModule() {

    @Provides @ActivityScope
    fun providesPodcastListPresenter() : PodcastListPresenter {
        return PodcastListPresenter()
    }
}
