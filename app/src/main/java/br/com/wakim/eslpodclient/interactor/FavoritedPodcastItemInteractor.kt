package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.onSuccessIfSubscribed
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single
import java.util.*

class FavoritedPodcastItemInteractor(private val podcastDbInteractor: PodcastDbInteractor, private val app: Application): PodcastInteractor(podcastDbInteractor, app) {

    companion object {
        final const val ITEMS_PER_PAGE = 20
    }

    fun addFavorite(podcastItem: PodcastItem): Single<Boolean> =
            Single.create<Boolean> { subscriber ->
                subscriber.onSuccessIfSubscribed(podcastDbInteractor.addFavorite(podcastItem))
            }

    fun removeFavorite(podcastItem: PodcastItem): Single<Boolean> =
            Single.create<Boolean> { subscriber ->
                subscriber.onSuccessIfSubscribed(podcastDbInteractor.removeFavorite(podcastItem))
            }

    fun getFavorites(page: Int, limit: Int): Single<List<PodcastItem>> =
            Single.create<List<PodcastItem>> { subscriber ->
                subscriber.onSuccessIfSubscribed(podcastDbInteractor.getFavorites(page, limit))
            }

    override fun getPodcasts(nextPageToken: String?) : Single<PodcastList> =
            getFavorites(nextPageToken?.toInt() ?: 0, ITEMS_PER_PAGE)
                    .map { list ->
                        val currentPageToken = nextPageToken?.toInt()
                        val podcastList = PodcastList(currentPageToken.toString(), ((currentPageToken ?: 0) + 1).toString())

                        podcastList.list = list as? ArrayList<PodcastItem> ?: ArrayList(list)

                        if (podcastList.list.size < ITEMS_PER_PAGE) {
                            podcastList.nextPageToken = null
                        }

                        podcastList
                    }
}