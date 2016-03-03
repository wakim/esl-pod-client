package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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

    companion object {
        final const val MINIMUM_THRESHOLD = 5
    }

    private var _hasMore : Boolean = false

    override var hasMore: Boolean
        get() = _hasMore
        set(value) {
            _hasMore = value
        }

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
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager : LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager

                val totalItemCount = linearLayoutManager.itemCount
                val lastVisible = linearLayoutManager.findLastVisibleItemPosition()

                val mustLoadMore = totalItemCount <= (lastVisible + MINIMUM_THRESHOLD)

                if (mustLoadMore && _hasMore && !adapter.loading) {
                    presenter!!.loadNextPage()
                }
            }
        })
    }

    override fun addItems(list: ArrayList<PodcastItem>) {
        adapter.addAll(list)
    }

    override fun setLoading(loading: Boolean) {
        adapter.loading = loading
    }
}