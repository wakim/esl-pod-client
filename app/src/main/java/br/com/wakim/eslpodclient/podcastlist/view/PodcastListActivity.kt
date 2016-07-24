package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.dagger.module.PodcastPlayerModule
import br.com.wakim.eslpodclient.extensions.snack
import br.com.wakim.eslpodclient.extensions.startActivity
import br.com.wakim.eslpodclient.podcastlist.downloaded.view.DownloadedListFragment
import br.com.wakim.eslpodclient.podcastlist.favorited.view.FavoritedListFragment
import br.com.wakim.eslpodclient.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.settings.view.SettingsActivity
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import butterknife.BindView
import it.sephiroth.android.library.bottomnavigation.BottomNavigation

open class PodcastListActivity : BaseActivity() {

    companion object {
        const val MINIMUM_THRESHOLD = 5
        const val FRAGMENT_TAG = "FRAGMENT"
    }

    private var podcastPlayerComponent: PodcastPlayerComponent? = null

    @BindView(R.id.coordinator_layout)
    lateinit var coordinatorLayout: CoordinatorLayout

    @BindView(R.id.appbar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.player_view)
    lateinit var playerView: ListPlayerView

    @BindView(R.id.fab_play)
    lateinit var playFab: FloatingActionButton

    @BindView(R.id.fab_pause)
    lateinit var pauseFab: FloatingActionButton

    @BindView(R.id.fab_loading)
    lateinit var loadingFab: LoadingFloatingActionButton

    @BindView(R.id.bottom_navigation)
    lateinit var bottomBar: BottomNavigation

    var podcastListFragment: PodcastListFragment? = null
    var downloadedListFragment: DownloadedListFragment? = null
    var favoritedListFragment: FavoritedListFragment? = null

    var currentFragment: PodcastListFragment? = null

    var savedState = arrayOfNulls<Fragment.SavedState>(3)

    var lastSelectedPosition = 0
    var lastOffset: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        createActivityComponent()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_podcastlist)
        createPlayerComponent()

        restoreFragmentStates(savedInstanceState)

        configureAppBarLayout()
        configureBottomBar()
        addFragmentIfNeeded()

        playerView.setControls(playFab, pauseFab, loadingFab)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        val fragmentManager = supportFragmentManager

        podcastListFragment?.let {
            if (it.isAdded) {
                fragmentManager.putFragment(outState!!, "PODCAST_LIST", it)
            }
        }

        favoritedListFragment?.let {
            if (it.isAdded) {
                fragmentManager.putFragment(outState!!, "FAVORITED_LIST", it)
            }
        }

        downloadedListFragment?.let {
            if (it.isAdded) {
                fragmentManager.putFragment(outState!!, "DOWNLOADED_LIST", it)
            }
        }
    }

    fun configureAppBarLayout() {
        appBarLayout.addOnOffsetChangedListener { appBarLayout, offset ->
            currentFragment?.let {
                if (offset == 0) {
                    it.setSwipeRefreshEnabled(true)
                } else if (lastOffset == 0 && it.isSwipeRefreshEnabled()) {
                    it.setSwipeRefreshEnabled(false)
                }
            }

            lastOffset = offset
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.podcast_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_settings) {
            startActivity<SettingsActivity>()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun restoreFragmentStates(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            val fragmentManager = supportFragmentManager

            podcastListFragment    = fragmentManager.getFragment(it, "PODCAST_LIST")    as? PodcastListFragment
            favoritedListFragment  = fragmentManager.getFragment(it, "FAVORITED_LIST")  as? FavoritedListFragment
            downloadedListFragment = fragmentManager.getFragment(it, "DOWNLOADED_LIST") as? DownloadedListFragment
        }
    }

    fun configureBottomBar() {
        bottomBar.setOnMenuItemClickListener(object: BottomNavigation.OnMenuItemSelectionListener {
            override fun onMenuItemSelect(id: Int, position: Int) {
                when (position) {
                    0 -> replaceListFragment(PodcastListFragment(), lastSelectedPosition, position)
                    1 -> replaceFavoritedFragment(FavoritedListFragment(), lastSelectedPosition, position)
                    2 -> replaceDownloadedFragment(DownloadedListFragment(), lastSelectedPosition, position)
                }

                lastSelectedPosition = position
            }

            override fun onMenuItemReselect(p0: Int, p1: Int) { }
        })
    }

    fun addFragmentIfNeeded() {
        currentFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as PodcastListFragment?

        if (currentFragment == null) {
            podcastListFragment = PodcastListFragment()

            currentFragment = podcastListFragment

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, podcastListFragment, FRAGMENT_TAG)
                    .commit()
        }
    }

    fun replaceListFragment(fragment: PodcastListFragment, previousPosition: Int, position: Int) {
        podcastListFragment = fragment
        replaceFragment(fragment, previousPosition, position)
    }

    fun replaceFavoritedFragment(fragment: FavoritedListFragment, previousPosition: Int, position: Int) {
        favoritedListFragment = favoritedListFragment
        replaceFragment(fragment, previousPosition, position)
    }

    fun replaceDownloadedFragment(fragment: DownloadedListFragment, previousPosition: Int, position: Int) {
        downloadedListFragment = fragment
        replaceFragment(fragment, previousPosition, position)
    }

    fun replaceFragment(fragment: PodcastListFragment, previousPosition: Int, position: Int) {
        val fragmentManager = supportFragmentManager
        val previousFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        val state = savedState[position]

        savedState[previousPosition] = fragmentManager.saveFragmentInstanceState(previousFragment)

        state?.let {
            fragment.setInitialSavedState(state)
        }

        currentFragment = fragment

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_TAG)
                .commit()
    }

    override fun getSystemService(name: String?): Any? {
        if (name == PodcastPlayerComponent::class.java.simpleName) {
            return podcastPlayerComponent
        }

        return super.getSystemService(name)
    }

    fun createPlayerComponent() {
        podcastPlayerComponent = activityComponent + PodcastPlayerModule(playerView)
    }

    override fun onBackPressed() {
        with (playerView) {
            if (isExpanded()) {
                collapse()
                return
            }

            if (isVisible()) {
                hide()
                return
            }
        }

        disposePlayerIfNeeded()

        super.onBackPressed()
    }

    override fun finish() {
        disposePlayerIfNeeded()
        super.finish()
    }

    open fun disposePlayerIfNeeded() {
        if (!playerView.isPlaying()) {
            playerView.explicitlyStop()
        }
    }

    override fun showMessage(messageResId: Int): Snackbar = snack(coordinatorLayout, messageResId)
}