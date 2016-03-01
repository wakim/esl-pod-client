package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.view.BaseActivity
import javax.inject.Inject

/**
 * Created by wakim on 2/29/16.
 */
class PodcastListActivity : BaseActivity<PodcastListPresenter>(), PodcastListView {

    @Inject
    fun injectPresenter(presenter : PodcastListPresenter) {
        this.presenter = presenter
        this.presenter?.view = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent.inject(this)
    }
}