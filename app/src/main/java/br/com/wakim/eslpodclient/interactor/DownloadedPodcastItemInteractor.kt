package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.onSuccessIfSubscribed
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastList
import rx.Single
import java.util.*

class DownloadedPodcastItemInteractor(private val podcastDbInteractor: PodcastDbInteractor, app: Application): PodcastInteractor(podcastDbInteractor, app) {
    companion object {
        const val ITEMS_PER_PAGE = 20
    }

    fun getDownloaded(page: Int, limit: Int): Single<List<PodcastItem>> =
            Single.create<List<PodcastItem>> { subscriber ->
                subscriber.onSuccessIfSubscribed(podcastDbInteractor.getDownloaded(page, limit))
            }

    override fun getCachedPodcasts(nextPageToken: String?): Single<PodcastList> = getPodcasts(nextPageToken)

    override fun getPodcasts(nextPageToken: String?): Single<PodcastList> =
            getDownloaded(nextPageToken?.toInt() ?: 0, ITEMS_PER_PAGE)
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