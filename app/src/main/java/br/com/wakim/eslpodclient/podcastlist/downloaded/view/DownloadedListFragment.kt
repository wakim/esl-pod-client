package br.com.wakim.eslpodclient.podcastlist.downloaded.view

import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.downloaded.presenter.DownloadedListPresenter
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListFragment
import javax.inject.Inject

class DownloadedListFragment: PodcastListFragment(), DownloadedListView {

    var synchronizeMenuItem: MenuItem? = null

    init {
        setHasOptionsMenu(true)
    }

    @Inject
    fun injectPresenter(presenter : DownloadedListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.downloaded_menu, menu)

        synchronizeMenuItem = menu?.findItem(R.id.synchronize)

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.synchronize) {
            (presenter as DownloadedListPresenter).synchronize()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun inject() =
            (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

    override fun showPopupMenuFor(podcastItem: PodcastItem, anchor: View) {
        val popupMenu = PopupMenu(context, anchor)

        popupMenu.inflate(R.menu.downloaded_podcast_item_menu)

        popupMenu.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.share                      -> share(podcastItem)
                R.id.favorite                   -> favorite(podcastItem)
                R.id.remove_download            -> removeDownload(podcastItem)
                R.id.remove_and_delete_download -> removeAndDeleteDownload(podcastItem)
                R.id.open_with                  -> openWith(podcastItem)
            }

            true
        }

        popupMenu.show()
    }

    fun removeDownload(podcastItem: PodcastItem) {
        presenter.removeDownload(podcastItem)
    }

    fun removeAndDeleteDownload(podcastItem: PodcastItem) {
        presenter.removeAndDeleteDownload(podcastItem)
    }

    override fun setSynchronizeMenuVisible(visible: Boolean) {
        synchronizeMenuItem?.isVisible = visible
    }

    override fun showAppBarLoading(loading: Boolean) {
    }
}