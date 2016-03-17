package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.extensions.insertIgnoringConflict
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemRowParser
import br.com.wakim.eslpodclient.model.PodcastList
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.threeten.bp.ZonedDateTime
import rx.Single
import java.util.*
import java.util.concurrent.TimeUnit

class PodcastItemFavoritesInteractor(private val app: Application): PodcastInteractor(app) {

    companion object {
        final const val ITEMS_PER_PAGE = 20
    }

    fun addFavorite(podcastItem: PodcastItem): Single<Long> =
            Single.create<Long> { subscriber ->
                app.database
                    .use {
                        val favoritedDate = ZonedDateTime.now().toEpochSecond()

                        with (podcastItem) {
                            val id = insertIgnoringConflict(
                                    DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                    "remote_id" to remoteId,
                                    "title" to title,
                                    "blurb" to blurb,
                                    "mp3_url" to mp3Url,
                                    "date" to date?.toEpochDay(),
                                    "tags" to tags,
                                    "type" to type,
                                    "favorited_date" to favoritedDate)

                            if (!subscriber.isUnsubscribed) {
                                subscriber.onSuccess(id)
                            }
                        }
                    }
            }

    fun removeFavorite(podcastItem: PodcastItem): Single<Boolean>  =
            Single.create<Boolean> { subscriber ->
                app.database
                        .use {
                            val deleted = delete(
                                    DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                    "remote_id = ?", arrayOf(podcastItem.remoteId.toString())
                            ) > 0

                            if (!subscriber.isUnsubscribed) {
                                subscriber.onSuccess(deleted)
                            }
                        }
            }

    fun getFavorites(page: Int, limit: Int): Single<List<PodcastItem>> =
            Single.create<List<PodcastItem>> { subscriber ->
                val list = app.database
                        .use {
                            select(
                                DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                "remote_id",
                                "title",
                                "blurb",
                                "mp3_url",
                                "date",
                                "tags",
                                "type"
                            )
                            .orderBy("date", SqlOrderDirection.DESC)
                            .limit(page * limit, limit)
                            .exec {
                                parseList(PodcastItemRowParser())
                            }
                        }

                if (!subscriber.isUnsubscribed) {
                    subscriber.onSuccess(list)
                }
            }

    override fun getPodcasts(nextPageToken: String?) : Single<PodcastList> =
            getFavorites(nextPageToken?.toInt() ?: 0, ITEMS_PER_PAGE)
                    .delay(5, TimeUnit.SECONDS)
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