package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.adapter.PodcastListAdapter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.view.BaseActivity
import butterknife.bindView
import java.util.*
import javax.inject.Inject

class PodcastListActivity : BaseActivity<PodcastListPresenter>(), PodcastListView {

    val recyclerView : RecyclerView by bindView(R.id.recycler_view)

    lateinit var adapter : PodcastListAdapter

    @Inject
    fun injectPresenter(presenter : PodcastListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcastlist)

        activityComponent.inject(this)
        adapter = PodcastListAdapter(this)

        recyclerView.adapter = adapter
    }

    override fun addItems(list: ArrayList<PodcastItem>) {
        adapter.addAll(list)
    }
}