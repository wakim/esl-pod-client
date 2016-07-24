package br.com.wakim.eslpodclient.dagger.module

import br.com.wakim.eslpodclient.dagger.scope.PodcastPlayerScope
import br.com.wakim.eslpodclient.ui.podcastplayer.view.ListPlayerView
import dagger.Module
import dagger.Provides

@Module
class PodcastPlayerModule(private val listPlayerView: ListPlayerView) {

    @Provides @PodcastPlayerScope
    fun providesListPlayerView() = listPlayerView
}