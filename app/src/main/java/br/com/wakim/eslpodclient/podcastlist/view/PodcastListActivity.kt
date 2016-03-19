package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.dp
import br.com.wakim.eslpodclient.extensions.isVisible
import br.com.wakim.eslpodclient.extensions.makeVisible
import br.com.wakim.eslpodclient.extensions.snack
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.adapter.PodcastListAdapter
import br.com.wakim.eslpodclient.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.view.BasePresenterActivity
import br.com.wakim.eslpodclient.widget.BottomSpacingItemDecoration
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import br.com.wakim.eslpodclient.widget.SpacingItemDecoration
import butterknife.bindView
import java.util.*
import javax.inject.Inject

open class PodcastListActivity : BasePresenterActivity<PodcastListPresenter>(), PodcastListView {

    companion object {
        final const val MINIMUM_THRESHOLD = 5
    }

    override var hasMore: Boolean = false

    val coordinatorLayout: CoordinatorLayout by bindView(R.id.coordinator_layout)
    val recyclerView: RecyclerView by bindView(R.id.recycler_view)
    val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)

    val playerView: ListPlayerView by bindView(R.id.player_view)

    val playFab: FloatingActionButton by bindView(R.id.fab_play)
    val pauseFab: FloatingActionButton by bindView(R.id.fab_pause)
    val loadingFab: LoadingFloatingActionButton by bindView(R.id.fab_loading)

    var toast: Toast? = null

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

        setupView()

        inject()

        configureAdapter()
        configureRecyclerView()

        swipeRefresh.setOnRefreshListener {
            adapter.removeAll()
            presenter.onRefresh()

            swipeRefresh.isRefreshing = false
        }

        playerView.setControls(playFab, pauseFab, loadingFab)
    }

    open fun inject() {
        activityComponent.inject(this)
    }

    open fun setupView() {
        setContentView(R.layout.activity_podcastlist)
    }

    fun share(podcastItem: PodcastItem) {
        presenter.share(podcastItem)
    }

    fun favorite(podcastItem: PodcastItem) {
        presenter.favorite(podcastItem)
    }

    fun download(podcastItem: PodcastItem) {
        presenter.download(podcastItem)
    }

    fun openWith(podcastItem: PodcastItem) {
        presenter.openWith(podcastItem)
    }

    fun configureAdapter() {
        adapter = PodcastListAdapter(this)
        adapter.clickListener = { podcastItem ->
            showPlayerViewIfNeeded()
            playerView.play(podcastItem)
        }

        adapter.overflowMenuClickListener = { podcastItem, anchor ->
            showPopupMenuFor(podcastItem, anchor)
        }
    }

    fun configureRecyclerView() {
        val hSpacing = dp(4)
        val vSpacing = dp(2)

        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(SpacingItemDecoration(hSpacing, vSpacing, hSpacing, vSpacing))
        recyclerView.addItemDecoration(bottomSpacingDecoration)

        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager : LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager

                val totalItemCount = linearLayoutManager.itemCount
                val lastVisible = linearLayoutManager.findLastVisibleItemPosition()

                val mustLoadMore = totalItemCount <= (lastVisible + MINIMUM_THRESHOLD)

                if (mustLoadMore && hasMore && !adapter.loading) {
                    presenter.loadNextPage()
                }
            }
        })
    }

    open fun showPopupMenuFor(podcastItem: PodcastItem, anchor: View) {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.inflate(R.menu.podcast_item_menu)

        popupMenu.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.share     -> share(podcastItem)
                R.id.favorite  -> favorite(podcastItem)
                R.id.download  -> download(podcastItem)
                R.id.open_with -> openWith(podcastItem)
            }

            true
        }

        popupMenu.show()
    }

    fun showPlayerViewIfNeeded() {
        if (playerView.isVisible()) {
            return
        }

        playerView.makeVisible()

        bottomSpacingDecoration.bottomSpacing = dp(72)
        recyclerView.invalidateItemDecorations()
    }

    override fun addItems(list: ArrayList<PodcastItem>) {
        adapter.addAll(list)
    }

    override fun remove(podcastItem: PodcastItem) {
        adapter.remove(podcastItem)
    }

    override fun setLoading(loading: Boolean) {
        adapter.loading = loading
    }

    override fun onSupportNavigateUp(): Boolean {
        playerView.explicitlyStop()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (playerView.isExpanded()) {
            playerView.collapse()
            return
        }

        if (playerView.isPlaying()) {
            if (!(toast?.isVisible() ?: false)) {
                toast = Toast.makeText(this, R.string.press_back_again_to_leave, Toast.LENGTH_LONG);
                toast!!.show()

                return
            }
        }

        disposePlayer()
        super.onBackPressed()
    }

    override fun finish() {
        disposePlayer()
        super.finish()
    }

    open fun disposePlayer() {
        playerView.explicitlyStop()
    }

    override fun showMessage(messageResId: Int): Snackbar = snack(coordinatorLayout, messageResId)
}