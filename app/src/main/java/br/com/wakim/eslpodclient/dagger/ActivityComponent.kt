package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.dagger.module.PresenterModule
import br.com.wakim.eslpodclient.dagger.scope.ActivityScope
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListActivity
import dagger.Component

/**
 * Created by wakim on 2/29/16.
 */
@ActivityScope
@Component(modules = arrayOf(PresenterModule::class))
interface ActivityComponent {
    fun inject(podcastListActivity: PodcastListActivity)
}
