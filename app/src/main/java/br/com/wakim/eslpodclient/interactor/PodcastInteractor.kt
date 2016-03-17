package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.model.PodcastList
import org.jetbrains.anko.db.IntParser
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.replace
import org.jetbrains.anko.db.select
import rx.Single

open class PodcastInteractor(private val app: Application) {

    open fun getPodcasts(nextPageToken: String?) : Single<PodcastList> =
            Single.create(PodcastListOnSubscribe(nextPageToken ?: BuildConfig.BASE_URL))

    open fun getPodcastDetail(podcastItem : PodcastItem) : Single<PodcastItemDetail> =
            Single.create(PodcastDetailOnSubscribe(podcastItem, BuildConfig.DETAIL_URL.format(podcastItem.remoteId.toString())))

    fun insertLastSeekPos(remoteId: Long, seekPos: Int) {
        Single.create<Long> { subscriber ->
            app.database
                    .use {
                        val id = replace(
                                DatabaseOpenHelper.SEEK_POSITIONS_TABLE_NAME,
                                "remote_id" to remoteId,
                                "last_seek_pos" to seekPos
                        )

                        subscriber.onSuccess(id)
                    }
        }.ofIOToMainThread().subscribe()
    }

    fun getLastSeekPos(remoteId: Long): Single<Int?> =
            Single.create { subscriber ->
                val lastSeekPos =
                        app.database
                                .use {
                                    select(DatabaseOpenHelper.SEEK_POSITIONS_TABLE_NAME, "last_seek_pos")
                                    .where("remote_id={remote_id}", "remote_id" to remoteId)
                                    .exec {
                                        parseOpt(IntParser)
                                    }
                                }

                if (!subscriber.isUnsubscribed) {
                    subscriber.onSuccess(lastSeekPos)
                }
            }
}
