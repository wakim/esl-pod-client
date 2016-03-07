package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class InteractorModule {
    @Provides @Singleton
    open fun providesPodcastListInteractor() : PodcastInteractor = PodcastInteractor()
}