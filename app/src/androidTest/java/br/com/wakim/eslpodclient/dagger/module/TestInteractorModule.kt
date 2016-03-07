package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.TestPodcastInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestInteractorModule : InteractorModule() {

    @Provides @Singleton
    override fun providesPodcastListInteractor() : PodcastInteractor = TestPodcastInteractor()
}