package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.InteractorModule
import br.com.wakim.eslpodclient.podcastlist.interactor.PodcastListInteractor
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(InteractorModule::class))
interface AppComponent {
    fun podcastListInteractor() : PodcastListInteractor

    fun plus() : ActivityComponent
}