package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.ActivityModule
import br.com.wakim.eslpodclient.dagger.module.PodcastPlayerModule
import br.com.wakim.eslpodclient.dagger.module.PresenterModule
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.ui.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.ui.settings.view.SettingsFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = arrayOf(PresenterModule::class, ActivityModule::class))
interface ActivityComponent {
    fun inject(settingsFragment: SettingsFragment)
    fun inject(playerView: ListPlayerView)

    operator fun plus(podcastPlayerModule: PodcastPlayerModule): PodcastPlayerComponent
}
