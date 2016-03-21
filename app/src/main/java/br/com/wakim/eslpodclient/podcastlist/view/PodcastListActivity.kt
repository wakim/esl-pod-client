package br.com.wakim.eslpodclient.podcastlist.view

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.widget.Toast
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.dagger.module.PodcastPlayerModule
import br.com.wakim.eslpodclient.extensions.isVisible
import br.com.wakim.eslpodclient.extensions.snack
import br.com.wakim.eslpodclient.podcastlist.downloaded.view.DownloadedListFragment
import br.com.wakim.eslpodclient.podcastlist.favorited.view.FavoritedListFragment
import br.com.wakim.eslpodclient.podcastplayer.view.ListPlayerView
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import butterknife.bindView

open class PodcastListActivity : BaseActivity() {

    companion object {
        final const val MINIMUM_THRESHOLD = 5
        final const val FRAGMENT_TAG = "FRAGMENT"
    }

    private var podcastPlayerComponent: PodcastPlayerComponent? = null

    val coordinatorLayout: CoordinatorLayout by bindView(R.id.coordinator_layout)

    val playerView: ListPlayerView by bindView(R.id.player_view)

    val playFab: FloatingActionButton by bindView(R.id.fab_play)
    val pauseFab: FloatingActionButton by bindView(R.id.fab_pause)
    val loadingFab: LoadingFloatingActionButton by bindView(R.id.fab_loading)

    var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        setupView()
        createPlayerComponent()

        playerView.setControls(playFab, pauseFab, loadingFab)

        addFragmentIfNeeded()
    }

    fun addFragmentIfNeeded() {
        var podcastListFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)

        if (podcastListFragment == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, PodcastListFragment(), FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onNavigationMenuItemSelected(itemId: Int) {
        when (itemId) {
            R.id.drawer_downloaded -> replaceFragment(DownloadedListFragment())
            R.id.drawer_favorites  -> replaceFragment(FavoritedListFragment())
        }
    }

    fun replaceFragment(fragment: PodcastListFragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG)
    }

    override fun getSystemService(name: String?): Any? {
        if (name == PodcastPlayerComponent::class.java.simpleName) {
            return podcastPlayerComponent
        }

        return super.getSystemService(name)
    }

    fun createPlayerComponent() {
        podcastPlayerComponent = activityComponent.plus(PodcastPlayerModule(playerView))
    }

    open fun setupView() {
        setContentView(R.layout.activity_podcastlist)
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