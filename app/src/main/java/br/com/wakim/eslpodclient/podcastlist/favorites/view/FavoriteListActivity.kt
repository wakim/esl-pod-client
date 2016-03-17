package br.com.wakim.eslpodclient.podcastlist.favorites.view

import android.support.v7.widget.PopupMenu
import android.view.View
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.favorites.presenter.FavoriteListPresenter
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListActivity
import javax.inject.Inject

class FavoriteListActivity: PodcastListActivity() {

    override fun setupView() {
        setContentView(R.layout.activity_podcastfavoritelist)
    }

    @Inject
    fun injectPresenter(presenter : FavoriteListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun inject() {
        activityComponent.inject(this)
    }

    override fun showPopupMenuFor(podcastItem: PodcastItem, anchor: View) {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.inflate(R.menu.favorited_podcast_item_menu)

        popupMenu.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.share           -> share(podcastItem)
                R.id.remove_favorite -> removeFavorite(podcastItem)
                R.id.download        -> download(podcastItem)
                R.id.open_with       -> openWith(podcastItem)
            }

            true
        }

        popupMenu.show()
    }

    fun removeFavorite(podcastItem: PodcastItem) {
        presenter!!.removeFavorite(podcastItem)
    }
}