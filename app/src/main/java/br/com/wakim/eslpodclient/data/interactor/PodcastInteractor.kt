package br.com.wakim.eslpodclient.data.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.data.interactor.rx.PodcastDetailOnSubscribe
import br.com.wakim.eslpodclient.data.interactor.rx.PodcastListOnSubscribe
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.data.model.PodcastItemDetail
import br.com.wakim.eslpodclient.data.model.PodcastList
import br.com.wakim.eslpodclient.util.extensions.connected
import br.com.wakim.eslpodclient.util.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.util.extensions.onNextIfSubscribed
import br.com.wakim.eslpodclient.util.extensions.onSuccessIfSubscribed
import rx.Observable
import rx.Single
import java.util.*

open class PodcastInteractor(private val podcastDbInteractor: PodcastDbInteractor, private val app: Application) {

    companion object {
        const val ITEMS_PER_PAGE = 20
    }

    open fun getPodcasts(nextPageToken: String?) : Single<PodcastList> =
            Single.create(PodcastListOnSubscribe(nextPageToken ?: BuildConfig.BASE_URL))
                    .doOnSuccess { podcastList ->
                        if (nextPageToken == null) {
                            podcastDbInteractor.clearPodcasts()
                        }

                        podcastDbInteractor.insertPodcasts(podcastList.list)
                    }
                    .connected()

    open fun getCachedPodcasts(nextPageToken: String?) : Single<PodcastList> =
            Single.create<List<PodcastItem>> { subscriber ->
                        val page = nextPageToken?.toInt() ?: 0
                        subscriber.onSuccessIfSubscribed(podcastDbInteractor.getPodcasts(page, ITEMS_PER_PAGE))
                    }
                    .map { list ->
                        val currentPageToken = nextPageToken?.toInt()
                        val podcastList = PodcastList(currentPageToken.toString(), ((currentPageToken ?: 0) + 1).toString())

                        podcastList.list = list as? ArrayList<PodcastItem> ?: ArrayList(list)

                        if (podcastList.list.size < DownloadedPodcastItemInteractor.ITEMS_PER_PAGE) {
                            podcastList.nextPageToken = null
                        }

                        podcastList
                    }

    open fun getPodcastDetail(podcastItem : PodcastItem) : Single<PodcastItemDetail> =
            podcastItem.let {
                return@let Observable.concat(createDbPodcastDetailObservable(it), createNetworkPodcastDetailObservable(it))
                        .first()
                        .toSingle()
            }

    private fun createDbPodcastDetailObservable(podcastItem: PodcastItem): Observable<PodcastItemDetail> =
        Observable.create { subscriber ->
            val podcastDetail = podcastDbInteractor.getPodcastDetail(podcastItem)

            if (podcastDetail != null) {
                subscriber.onNextIfSubscribed(podcastDetail)
            }

            subscriber.onCompleted()
        }


    private fun createNetworkPodcastDetailObservable(podcastItem: PodcastItem): Observable<PodcastItemDetail> =
        Single.create(PodcastDetailOnSubscribe(podcastItem, BuildConfig.DETAIL_URL.format(podcastItem.remoteId.toString())))
                .doOnSuccess { podcastItemDetail ->
                    podcastDbInteractor.insertPodcastDetail(podcastItemDetail)
                }
                .connected()
                .toObservable()


    fun insertLastSeekPos(remoteId: Long, seekPos: Int) {
        Single.create<Boolean> { subscriber ->
            subscriber.onSuccessIfSubscribed(podcastDbInteractor.insertLastSeekPos(remoteId, seekPos))
        }.ofIOToMainThread().subscribe()
    }

    fun getLastSeekPos(remoteId: Long): Single<Int?> =
            Single.create { subscriber ->
                val lastSeekPos = podcastDbInteractor.getLastSeekPos(remoteId)

                subscriber.onSuccessIfSubscribed((lastSeekPos as? Long)?.toInt() ?: null)
            }
}
