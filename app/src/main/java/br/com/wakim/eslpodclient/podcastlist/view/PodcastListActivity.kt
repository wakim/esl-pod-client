package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.dp
import br.com.wakim.eslpodclient.extensions.isVisible
import br.com.wakim.eslpodclient.extensions.makeVisible
import br.com.wakim.eslpodclient.extensions.snack
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.adapter.PodcastListAdapter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.widget.BottomSpacingItemDecoration
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import br.com.wakim.eslpodclient.widget.SpacingItemDecoration
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

    val coordinatorLayout : CoordinatorLayout by bindView(R.id.coordinator_layout)
    val recyclerView : RecyclerView by bindView(R.id.recycler_view)
    val playerView: ListPlayerView by bindView(R.id.player_view)

    val playFab : FloatingActionButton by bindView(R.id.fab_play)
    val pauseFab : FloatingActionButton by bindView(R.id.fab_pause)
    val loadingFab : LoadingFloatingActionButton by bindView(R.id.fab_loading)

    val bottomSpacingDecoration = BottomSpacingItemDecoration(0)

    lateinit var adapter : PodcastListAdapter

    @Inject
    fun injectPresenter(presenter : PodcastListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_podcastlist)

        activityComponent.inject(this)

        configureAdapter()
        configureRecyclerView()

        playerView.setControls(playFab, pauseFab, loadingFab)
    }

    fun configureAdapter() {
        adapter = PodcastListAdapter(this)
        adapter.onClickListener = { podcastItem : PodcastItem ->
            showPlayerViewIfNeeded()
            playerView.play(podcastItem)
        }
    }

    fun configureRecyclerView() {
        val spacing = dp(8F).toInt()

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(bottomSpacingDecoration)
        recyclerView.addItemDecoration(SpacingItemDecoration(columns = 1, spacingTop = spacing, spacingBottom = spacing))

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

    fun showPlayerViewIfNeeded() {
        if (playerView.isVisible()) {
            return
        }

        playerView.makeVisible()

        bottomSpacingDecoration.bottomSpacing = dp(72F).toInt()
        recyclerView.invalidateItemDecorations()
    }

    override fun addItems(list: ArrayList<PodcastItem>) {
        adapter.addAll(list)
    }

    override fun setLoading(loading: Boolean) {
        adapter.loading = loading
    }

    override fun onSupportNavigateUp(): Boolean {
        playerView.explicitlyStop()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        playerView.explicitlyStop()
        super.onBackPressed()
    }

    override fun showMessage(messageResId: Int) {
        snack(coordinatorLayout, messageResId)
    }
}