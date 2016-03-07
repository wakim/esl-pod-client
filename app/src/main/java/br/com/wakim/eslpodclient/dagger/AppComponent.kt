package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.AppModule
import br.com.wakim.eslpodclient.dagger.module.InteractorModule
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.preference.PreferenceManager
import br.com.wakim.eslpodclient.service.PlayerService
import br.com.wakim.eslpodclient.service.StorageService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(InteractorModule::class, AppModule::class))
interface AppComponent {
    fun inject(playerService: PlayerService)
    fun inject(storageService: StorageService)

    fun podcastListInteractor() : PodcastInteractor
    fun prefencesManager() : PreferenceManager
    fun plus() : ActivityComponent
}