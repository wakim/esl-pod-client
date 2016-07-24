package br.com.wakim.eslpodclient.ui.podcastlist.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ShareCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.android.widget.BottomSpacingItemDecoration
import br.com.wakim.eslpodclient.android.widget.SpacingItemDecoration
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.ui.podcastlist.adapter.PodcastListAdapter
import br.com.wakim.eslpodclient.ui.podcastlist.presenter.PodcastListPresenter
import br.com.wakim.eslpodclient.ui.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.ui.view.BaseActivity
import br.com.wakim.eslpodclient.ui.view.BasePresenterFragment
import br.com.wakim.eslpodclient.util.browseWithCustomTabs
import br.com.wakim.eslpodclient.util.extensions.dp
import br.com.wakim.eslpodclient.util.extensions.makeVisible
import br.com.wakim.eslpodclient.util.extensions.startActivity
import butterknife.BindView
import java.util.*
import javax.inject.Inject

open class PodcastListFragment: BasePresenterFragment<PodcastListPresenter>(), PodcastListView {

    companion object {
        const val ITEMS_EXTRA = "ITEMS"
        const val NEXT_PAGE_TOKEN_EXTRA = "NEXT_PAGE_TOKEN"
        const val DOWNLOAD_PODCAST_EXTRA = "DOWNLOAD_PODCAST"
        const val USING_CACHE_EXTRA = "USING_CACHE"
    }

    override var hasMore: Boolean = false

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.swipe_refresh)
    lateinit var swipeRefresh: SwipeRefreshLayout

    val bottomSpacingDecoration = BottomSpacingItemDecoration(0)

    lateinit var adapter: PodcastListAdapter

    @Inject
    lateinit var baseActivity: BaseActivity

    @Inject
    lateinit var playerView: ListPlayerView

    @Inject
    fun injectPresenter(presenter: PodcastListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    open fun inject() =
            (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater?.inflate(R.layout.fragment_podcastlist, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        inject()
        super.onActivityCreated(savedInstanceState)

        savedInstanceState?.let {
            val items: ArrayList<PodcastItem> = it.getParcelableArrayList(ITEMS_EXTRA)
            val token: String? = it.getString(NEXT_PAGE_TOKEN_EXTRA)
            val downloadPodcast: PodcastItem? = it.getParcelable(DOWNLOAD_PODCAST_EXTRA)
            val usingCache = it.getBoolean(USING_CACHE_EXTRA)

            presenter.onRestoreInstanceState(items, token, downloadPodcast, usingCache)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            it.putParcelableArrayList(ITEMS_EXTRA, presenter.items)
            it.putString(NEXT_PAGE_TOKEN_EXTRA, presenter.nextPageToken)
            it.putParcelable(DOWNLOAD_PODCAST_EXTRA, presenter.downloadPodcastItem)
            it.putBoolean(USING_CACHE_EXTRA, presenter.usingCache)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureAdapter()
        configureRecyclerView()

        swipeRefresh.apply {
            this.setOnRefreshListener {
                adapter.removeAll()
                presenter.onRefresh()

                isRefreshing = false
            }
        }

        super.onViewCreated(view, savedInstanceState)
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
        adapter = PodcastListAdapter(context)

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

                val mustLoadMore = totalItemCount <= (lastVisible + PodcastListActivity.MINIMUM_THRESHOLD)

                if (mustLoadMore && hasMore && !adapter.loading) {
                    presenter.loadNextPage()
                }
            }
        })
    }

    open fun showPopupMenuFor(podcastItem: PodcastItem, anchor: View) {
        val popupMenu = PopupMenu(context, anchor)

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

    override fun setItems(list: ArrayList<PodcastItem>) {
        adapter.setItems(list)
    }

    override fun addItem(podcastItem: PodcastItem) {
        adapter.add(podcastItem)
    }

    override fun share(text: String?) {
        ShareCompat.IntentBuilder.from(baseActivity)
                .setText(text)
                .setType("text/plain")
                .createChooserIntent()
                .startActivity(baseActivity)
    }

    override fun openUrlOnBrowser(url: String) {
        baseActivity.browseWithCustomTabs(url)
    }

    override fun remove(podcastItem: PodcastItem) {
        adapter.remove(podcastItem)
    }

    override fun setLoading(loading: Boolean) {
        adapter.loading = loading
    }

    override fun showMessage(messageResId: Int): Snackbar = baseActivity.showMessage(messageResId)

    override fun showMessage(messageResId: Int, action: String, clickListener: (() -> Unit)?) =
            baseActivity.showMessage(messageResId, action, clickListener)

    fun isSwipeRefreshEnabled() = if (view == null) false else swipeRefresh.isEnabled

    fun setSwipeRefreshEnabled(enabled: Boolean) {
        if (view != null) {
            swipeRefresh.isEnabled = enabled
        }
    }
}
