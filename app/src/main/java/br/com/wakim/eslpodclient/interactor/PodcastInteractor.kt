package br.com.wakim.eslpodclient.interactor

import android.database.sqlite.SQLiteDatabase
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.extensions.LongAnyParser
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.extensions.toContentValues
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.model.PodcastList
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update
import rx.Single

open class PodcastInteractor(private val app: Application) {

    open fun getPodcasts(nextPageToken: String?) : Single<PodcastList> =
            Single .create(PodcastListOnSubscribe(nextPageToken ?: BuildConfig.BASE_URL))
                    .doOnSuccess { podcastList ->
                        insertPodcasts(podcastList.list)
                    }

    open fun getPodcastDetail(podcastItem : PodcastItem) : Single<PodcastItemDetail> =
            Single.create(PodcastDetailOnSubscribe(podcastItem, BuildConfig.DETAIL_URL.format(podcastItem.remoteId.toString())))

    fun insertPodcasts(podcasts: List<PodcastItem>) {
        app.database
                .use {
                    podcasts.asSequence().forEach { podcastItem ->
                        with (podcastItem) {
                            insertWithOnConflict(
                                    DatabaseOpenHelper.PODCASTS_TABLE_NAME,
                                    null,
                                    arrayOf(
                                            "remote_id" to remoteId,
                                            "title" to title,
                                            "blurb" to blurb,
                                            "mp3_url" to mp3Url,
                                            "date" to date?.toEpochDay(),
                                            "tags" to tags,
                                            "type" to type
                                            ).toContentValues(),
                                    SQLiteDatabase.CONFLICT_IGNORE)
                        }
                    }
                }
    }

    fun insertLastSeekPos(remoteId: Long, seekPos: Int) {
        Single.create<Boolean> { subscriber ->
            app.database
                    .use {
                        val updated = update(DatabaseOpenHelper.PODCASTS_TABLE_NAME, "last_seek_pos" to seekPos)
                                .where("remote_id = {remoteId}", "remoteId" to remoteId)
                                .exec() > 0

                        subscriber.onSuccess(updated)
                    }
        }.ofIOToMainThread().subscribe()
    }

    fun getLastSeekPos(remoteId: Long): Single<Int?> =
            Single.create { subscriber ->
                val lastSeekPos =
                        app.database
                                .use {
                                    select(DatabaseOpenHelper.PODCASTS_TABLE_NAME, "last_seek_pos")
                                    .where("remote_id = {remoteId}", "remoteId" to remoteId)
                                    .exec {
                                        parseOpt(LongAnyParser())
                                    }
                                }

                if (!subscriber.isUnsubscribed) {
                    subscriber.onSuccess((lastSeekPos as? Long)?.toInt() ?: null)
                }
            }
}
