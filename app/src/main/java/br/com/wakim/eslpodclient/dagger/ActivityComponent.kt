package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.ActivityModule
import br.com.wakim.eslpodclient.dagger.module.PresenterModule
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListActivity
import br.com.wakim.eslpodclient.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.settings.view.SettingsFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = arrayOf(PresenterModule::class, ActivityModule::class))
interface ActivityComponent {
    fun inject(podcastListActivity: PodcastListActivity)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(playerView: ListPlayerView)
}
