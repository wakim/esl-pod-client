package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.PresenterModule
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.podcastlist.view.ListPlayerView
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListActivity
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = arrayOf(PresenterModule::class))
interface ActivityComponent {
    fun inject(podcastListActivity: PodcastListActivity)

    fun inject(playerView: ListPlayerView)
}
