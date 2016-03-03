package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.podcastlist.interactor.PodcastListInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InteractorModule {

    @Provides @Singleton
    fun providesPodcastListInteractor() : PodcastListInteractor = PodcastListInteractor()
}