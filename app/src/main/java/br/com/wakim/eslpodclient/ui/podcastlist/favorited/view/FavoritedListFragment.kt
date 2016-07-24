package br.com.wakim.eslpodclient.ui.podcastlist.favorited.view

import android.support.v7.widget.PopupMenu
import android.view.View
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.ui.podcastlist.favorited.presenter.FavoritedListPresenter
import br.com.wakim.eslpodclient.ui.podcastlist.view.PodcastListFragment
import javax.inject.Inject

class FavoritedListFragment: PodcastListFragment() {

    @Inject
    fun injectPresenter(presenter : FavoritedListPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun inject() =
            (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

    override fun showPopupMenuFor(podcastItem: PodcastItem, anchor: View) {
        val popupMenu = PopupMenu(context, anchor)

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
        presenter.removeFavorite(podcastItem)
    }
}